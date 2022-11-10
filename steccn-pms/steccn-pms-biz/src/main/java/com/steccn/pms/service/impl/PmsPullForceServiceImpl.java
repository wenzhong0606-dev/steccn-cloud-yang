package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.mapper.PmsPullForceMapper;
import com.steccn.pms.vo.PmsPullForceClassQuantity;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@RequiredArgsConstructor
@Service
public class PmsPullForceServiceImpl extends ServiceImpl<PmsPullForceMapper, PmsPullForce>
        implements PmsPullForceService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public List<PmsPullForceVO> list(Integer workshopId, String monthDate) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getWorkshopId, workshopId)
                .like(PmsPullForce::getPullForceDate, monthDate));

        //根据日期分组
        Map<LocalDate, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getPullForceDate));
        //获取白班和夜班的id集合
        Set<Long> classIdSet = pmsPullForceList.stream().map(PmsPullForce::getClassesId).collect(Collectors.toSet());
        Map<Long, SysDictVO> classIdMap = remoteDictFeignApi.listById(new ArrayList<>(classIdSet)).getData();


        List<PmsPullForceVO> pmsPullForceVOList = new ArrayList<>();
        //组装返回参数
        for (Map.Entry<LocalDate, List<PmsPullForce>> entry : groupBy.entrySet()) {
            PmsPullForceVO pullForceVO = new PmsPullForceVO();
            pullForceVO.setPullForceDate(entry.getKey());
            pullForceVO.setWorkshopId(entry.getValue().get(0).getWorkshopId());

            List<PmsPullForce> localDateGroupList = entry.getValue();
            //再按班次分组
            Map<Long, List<PmsPullForce>> classMap = localDateGroupList.stream().collect(Collectors.groupingBy(PmsPullForce::getClassesId));
            for (Map.Entry<Long, List<PmsPullForce>> classEntry : classMap.entrySet()) {
                //获取白班夜班总量
                BigDecimal dayClassTotal = classEntry.getValue().stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                String classCode = classIdMap.get(classEntry.getKey()).getCode();

                if (SysDictConstant.CODE_0.equals(classCode)) {
                    pullForceVO.setDayClassTotal(dayClassTotal);
                } else {
                    pullForceVO.setNightClassTotal(dayClassTotal);
                }
            }
            //获取总量
            BigDecimal total = localDateGroupList.stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            pullForceVO.setTotal(total);
            pmsPullForceVOList.add(pullForceVO);
        }
        List<LocalDate> localDateList = LocalDateUtil.getCurrentMonthDateAll(LocalDate.parse(monthDate + "-01", ISO_DATE));

        List<PmsPullForceVO> pullForceVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            PmsPullForceVO pmsPullForceVO = new PmsPullForceVO();
            Optional<PmsPullForceVO> present = pmsPullForceVOList.stream().filter(pullForceVO -> pullForceVO.getPullForceDate().equals(localDate)).findAny();
            if (present.isPresent()) {
                BeanUtils.copyProperties(present.get(), pmsPullForceVO);
                pmsPullForceVO.setUnitName(PmManagerConstant.UNIT_T);
            } else {
                pmsPullForceVO.setPullForceDate(localDate);
            }
            pullForceVOList.add(pmsPullForceVO);
        }

        return pullForceVOList;
    }

    @Override
    public List<PmsPullForceDetailsVO> details(Integer workshopId, String pullForceDate) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getWorkshopId, workshopId)
                .eq(PmsPullForce::getPullForceDate, pullForceDate));

        //根据产线分组
        Map<Integer, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        Set<Integer> productLineIdSet = pmsPullForceList.stream().map(PmsPullForce::getProductLineId).collect(Collectors.toSet());

        Map<Integer, SysOfficeVO> map = remoteOfficeFeign.listById(productLineIdSet).getData();
        List<SysDictVO> sysDictVOList = remoteDictFeignApi.listByType("sys_shift").getData();
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(workshopId);
        sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
        List<SysOfficeVO> sysOfficeVOList = remoteOfficeFeign.listByParentId(sysOfficeParentDTO).getData();

        List<PmsPullForceDetailsVO> pullForceDetailsVOList = new ArrayList<>();
        for (Map.Entry<Integer, List<PmsPullForce>> entry : groupBy.entrySet()) {
            BigDecimal total = entry.getValue().stream().map(PmsPullForce::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            PmsPullForceDetailsVO pullForceDetailsVO = new PmsPullForceDetailsVO();
            pullForceDetailsVO.setProductLineId(entry.getKey());
            pullForceDetailsVO.setProductLineName(map.get(entry.getKey()).getOfficeName());
            pullForceDetailsVO.setTotal(total);
            pullForceDetailsVO.setUnitName(PmManagerConstant.UNIT_T);

            //获取白班夜班数量
            List<PmsPullForceClassQuantity> pullForceClassQuantityList = new ArrayList<>();
            for (SysDictVO sysDictVO : sysDictVOList) {
                PmsPullForceClassQuantity pullForceClassQuantity = new PmsPullForceClassQuantity();

                Optional<PmsPullForce> present = entry.getValue().stream().filter(pmsPullForce -> pmsPullForce.getClassesId().equals(sysDictVO.getId())).findAny();
                if (present.isPresent()) {
                    pullForceClassQuantity.setId(present.get().getId());
                    pullForceClassQuantity.setClassId(present.get().getClassesId());
                    pullForceClassQuantity.setQuantity(present.get().getQuantity());
                } else {
                    pullForceClassQuantity.setClassId(sysDictVO.getId());
                }
                pullForceClassQuantityList.add(pullForceClassQuantity);
            }

            pullForceDetailsVO.setPullForceClassQuantityList(pullForceClassQuantityList);
            pullForceDetailsVOList.add(pullForceDetailsVO);
        }

        List<PmsPullForceDetailsVO> pmsPullForceDetailsVOList = new ArrayList<>();
        for (SysOfficeVO sysOfficeVO : sysOfficeVOList) {
            PmsPullForceDetailsVO pmsPullForceDetailsVO = new PmsPullForceDetailsVO();
            Optional<PmsPullForceDetailsVO> present = pullForceDetailsVOList.stream().filter(pullForceDetailsVO -> pullForceDetailsVO.getProductLineId().equals(sysOfficeVO.getId())).findAny();
            if (present.isPresent()) {
                pmsPullForceDetailsVOList.add(present.get());
            } else {
                pmsPullForceDetailsVO.setProductLineId(sysOfficeVO.getId());
                pmsPullForceDetailsVO.setProductLineName(sysOfficeVO.getOfficeName());
                pmsPullForceDetailsVOList.add(pmsPullForceDetailsVO);
            }
        }

        return pmsPullForceDetailsVOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean editBatch(List<PmsPullForceEditDTO> dtoList) {
        List<PmsPullForce> addList = new ArrayList<>();
        List<PmsPullForce> updateList = new ArrayList<>();
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode(PmManagerConstant.UNIT_T, PmManagerConstant.ENERGY_CONSUME).getData();

        for (PmsPullForceEditDTO pmsPullForceEditDTO : dtoList) {
            PmsPullForce pmsPullForce = new PmsPullForce();
            BeanUtils.copyProperties(pmsPullForceEditDTO, pmsPullForce);
            pmsPullForce.setUnitId(sysDictVO.getId());
            if (Objects.isNull(pmsPullForceEditDTO.getId())) {
                addList.add(pmsPullForce);
            } else {
                updateList.add(pmsPullForce);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            if (!saveBatch(addList)) {
                throw new BusinessException("拉引量新增失败");
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            if (!updateBatchById(updateList)) {
                throw new BusinessException("拉引量编辑失败");
            }
        }
        return true;
    }

    @Override
    public Map<Long, PmsPullForce> pullForceDay(Integer productLineId, LocalDate date) {
        Map<Long, PmsPullForce> resMap = Maps.newHashMap();
        if (Objects.nonNull(productLineId) && Objects.nonNull(date)) {
            List<PmsPullForce> list = list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getProductLineId, productLineId).eq(PmsPullForce::getPullForceDate, date));
            if (CollUtil.isNotEmpty(list)) {
                for (PmsPullForce pmsPullForce : list) {
                    resMap.put(pmsPullForce.getClassesId(), pmsPullForce);
                }
            }
        }
        return resMap;
    }
}




