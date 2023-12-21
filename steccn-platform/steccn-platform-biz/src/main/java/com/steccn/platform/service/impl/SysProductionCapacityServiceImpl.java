package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysConfigurationConf;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.JointUtil;
import com.steccn.platform.domain.SysProductionCapacity;
import com.steccn.platform.domain.SysProductionCapacityInfo;
import com.steccn.platform.dto.SysOfficeMultiTreeDTO;
import com.steccn.platform.dto.SysProductionCapacityAddDTO;
import com.steccn.platform.dto.SysProductionCapacityInfoDTO;
import com.steccn.platform.dto.SysProductionCapacityPageDTO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.mapper.SysProductionCapacityMapper;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.service.SysProductionCapacityInfoService;
import com.steccn.platform.service.SysProductionCapacityService;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysProductionCapacityInfoVO;
import com.steccn.platform.vo.SysProductionCapacityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_production_capacity(标准产能表)】的数据库操作Service实现
 * @createDate 2023-12-19 16:50:56
 */
@Service
@RequiredArgsConstructor
public class SysProductionCapacityServiceImpl extends ServiceImpl<SysProductionCapacityMapper, SysProductionCapacity>
        implements SysProductionCapacityService {

    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final SysOfficeService sysOfficeService;
    private final SysProductionCapacityInfoService sysProductionCapacityInfoService;

    @Override
    public Page<SysProductionCapacityVO> capacityPage(SysProductionCapacityPageDTO dto) {
        //获取日期
        LocalDate start = null, end = null;
        if (StrUtil.isNotBlank(dto.getMonthStr())) {
            String startStr = dto.getMonthStr() + "-01";
            start = LocalDate.parse(startStr);
            end = start.plusMonths(1);
        }
        //遍历数据
        Page<SysProductionCapacity> pageData = page(new Page<>(dto.getCurrent(), dto.getSize()),
                new LambdaQueryWrapper<SysProductionCapacity>()
                        .ge(Objects.nonNull(start), SysProductionCapacity::getClassDate, start)
                        .lt(Objects.nonNull(end), SysProductionCapacity::getClassDate, end));
        Page<SysProductionCapacityVO> resPage = new Page<>();
        BeanUtil.copyProperties(pageData, resPage);
        if (CollUtil.isNotEmpty(pageData.getRecords())) {
            List<SysProductionCapacityVO> resList = BeanUtil.copyToList(pageData.getRecords(), SysProductionCapacityVO.class);
            resPage.setRecords(resList);
        }
        return resPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCapacity(SysProductionCapacityAddDTO dto) {
        long exist = count(new LambdaQueryWrapper<SysProductionCapacity>().eq(SysProductionCapacity::getClassDate, dto.getClassDate()));
        Assert.isTrue(exist == 0, "当前日期已存在标准能耗");
        //获取配置的车间
        TripleDTO<String, Set<String>, Integer> tripleDTO = new TripleDTO<>(SysConfigurationConf.STANDARD_CAPACITY_WORKSHOP, Sets.newHashSet(SysConfigurationConf.WORKSHOP), 2);
        Map<String, List<String>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListString(tripleDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> workshopCodeList = confMap.get(SysConfigurationConf.WORKSHOP);
        Assert.notEmpty(workshopCodeList, "没有配置车间");
        //车间下产线下的所有工位
        SysOfficeMultiTreeDTO sysOfficeMultiTreeDTO = new SysOfficeMultiTreeDTO();
        sysOfficeMultiTreeDTO.setOfficeTypeList(Lists.newArrayList("8", "9", "13"));
        sysOfficeMultiTreeDTO.setRootCodeList(workshopCodeList);
        List<SysOfficeVO> sysOfficeVOList = sysOfficeService.multiTree(sysOfficeMultiTreeDTO);
        List<SysProductionCapacityInfo> infoList = filterStation(sysOfficeVOList);
        Assert.notEmpty(infoList, "车间下没有工位");
        //查询最近的工位默认配置
        SysProductionCapacity one = getOne(new LambdaQueryWrapper<SysProductionCapacity>().orderByDesc(SysProductionCapacity::getClassDate).last(MpConstant.STR_LIMIT_1));
        Map<String, Integer> defaultValueMap = Maps.newHashMap();
        if (Objects.nonNull(one)) {
            defaultValueMap = getDefault(one.getId());
        }
        //保存主表
        SysProductionCapacity mainModel = new SysProductionCapacity();
        BeanUtil.copyProperties(dto, mainModel);
        save(mainModel);
        //保存子表
        for (SysProductionCapacityInfo ele : infoList) {
            ele.setCapacityId(mainModel.getId());
            ele.setLineNum(defaultValueMap.getOrDefault(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, ele.getWorkshopId(), ele.getLineId(), ele.getStationId()), 0));
        }
        return sysProductionCapacityInfoService.saveBatch(infoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCapacity(Integer id) {
        removeById(id);
        return sysProductionCapacityInfoService.remove(new LambdaQueryWrapper<SysProductionCapacityInfo>().eq(SysProductionCapacityInfo::getCapacityId, id));
    }

    @Override
    public List<SysProductionCapacityInfoVO> infoCapacity(Integer id) {
        SysProductionCapacity exist = getById(id);
        Assert.notNull(exist, "产能不存在");
        List<SysProductionCapacityInfo> dbList = sysProductionCapacityInfoService.list(new LambdaQueryWrapper<SysProductionCapacityInfo>().eq(SysProductionCapacityInfo::getCapacityId, id));
        if (CollUtil.isNotEmpty(dbList)) {
            List<SysProductionCapacityInfoVO> infoList = BeanUtil.copyToList(dbList, SysProductionCapacityInfoVO.class);
            Set<Integer> wlsIdSet = Sets.newHashSet();
            infoList.forEach(ele -> {
                wlsIdSet.add(ele.getWorkshopId());
                wlsIdSet.add(ele.getLineId());
                wlsIdSet.add(ele.getStationId());
            });
            Map<Integer, String> wlsIdNameMap = sysOfficeService.listByCh(wlsIdSet);

            BigDecimal avgCapacity = BigDecimalUtil.divide(exist.getStandardCapacity(), new BigDecimal(exist.getLineNum()), 2);
            infoList.forEach(ele -> {
                ele.setStandardCapacity(BigDecimalUtil.multiply(avgCapacity, new BigDecimal(ele.getLineNum()), 2));
                ele.setWorkshopName(wlsIdNameMap.get(ele.getWorkshopId()));
                ele.setLineName(wlsIdNameMap.get(ele.getLineId()));
                ele.setStationName(wlsIdNameMap.get(ele.getStationId()));
            });
            return infoList;
        }
        return Lists.newArrayList();
    }

    @Override
    public Boolean updateCapacity(List<SysProductionCapacityInfoDTO> list) {
        List<SysProductionCapacityInfo> batchList = Lists.newArrayList();
        for (SysProductionCapacityInfoDTO ele : list) {
            SysProductionCapacityInfo info = new SysProductionCapacityInfo();
            BeanUtil.copyProperties(ele, info);
            batchList.add(info);
        }
        return sysProductionCapacityInfoService.updateBatchById(batchList);
    }

    /**
     * 过滤出工位
     *
     * @param sysOfficeVOList 组织结构树
     * @return
     */
    private List<SysProductionCapacityInfo> filterStation(List<SysOfficeVO> sysOfficeVOList) {
        List<SysProductionCapacityInfo> resList = Lists.newArrayList();
        sysOfficeVOList.forEach(workshop -> {
            if (CollUtil.isNotEmpty(workshop.getChildren())) {
                workshop.getChildren().forEach(line -> {
                    if (CollUtil.isNotEmpty(line.getChildren())) {
                        line.getChildren().forEach(station -> {
                            SysProductionCapacityInfo info = new SysProductionCapacityInfo();
                            info.setWorkshopId(workshop.getId());
                            info.setLineId(line.getId());
                            info.setStationId(station.getId());
                            resList.add(info);
                        });
                    }
                });

            }
        });
        return resList;
    }

    /**
     * 获取默认数据
     *
     * @param id 标准能耗主键
     * @return
     */
    private Map<String, Integer> getDefault(Integer id) {
        Map<String, Integer> resMap = Maps.newHashMap();
        List<SysProductionCapacityInfo> list = sysProductionCapacityInfoService.list(new LambdaQueryWrapper<SysProductionCapacityInfo>().eq(SysProductionCapacityInfo::getCapacityId, id));
        if (Objects.nonNull(list)) {
            list.forEach(ele -> resMap.put(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, ele.getWorkshopId(), ele.getLineId(), ele.getStationId()), ele.getLineNum()));
        }
        return resMap;

    }

}




