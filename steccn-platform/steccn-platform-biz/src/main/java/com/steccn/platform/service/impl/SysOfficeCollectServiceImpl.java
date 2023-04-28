package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.domain.SysOfficeCollect;
import com.steccn.platform.dto.SysOfficeCollectAddDTO;
import com.steccn.platform.dto.SysOfficeCollectPageDTO;
import com.steccn.platform.dto.SysOfficeCollectUpdateDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.service.SysOfficeCollectService;
import com.steccn.platform.mapper.SysOfficeCollectMapper;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.SysOfficeCollectVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_office_collect(工位配置)】的数据库操作Service实现
 * @createDate 2022-12-16 10:34:00
 */
@Service
@Slf4j
public class SysOfficeCollectServiceImpl extends ServiceImpl<SysOfficeCollectMapper, SysOfficeCollect>
        implements SysOfficeCollectService{

    @Autowired
    private SysOfficeService sysOfficeService;
    @Override
    public Page pageList(SysOfficeCollectPageDTO dto) {
        //查询条件
        LambdaQueryWrapper<SysOfficeCollect> condition = new LambdaQueryWrapper<SysOfficeCollect>()
                .eq(Objects.nonNull(dto.getOfficeType()), SysOfficeCollect::getOfficeType, dto.getOfficeType())
                .eq(Objects.nonNull(dto.getOfficeId()), SysOfficeCollect::getOfficeId, dto.getOfficeId())
                .eq(StringUtils.isNotBlank(dto.getCode()), SysOfficeCollect::getCode, dto.getCode())
                .eq(StringUtils.isNotBlank(dto.getName()), SysOfficeCollect::getName, dto.getName())
                .eq(Objects.nonNull(dto.getType()), SysOfficeCollect::getType, dto.getType());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, condition);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<SysOfficeCollectVO> res = BeanUtil.copyToList(resPage.getRecords(), SysOfficeCollectVO.class);

        return page.setRecords(res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysOfficeCollectAddDTO dto) {
        long number = count(new LambdaQueryWrapper<SysOfficeCollect>()
                .eq(SysOfficeCollect::getOfficeId, dto.getOfficeId())
                .eq(SysOfficeCollect::getOfficeType, dto.getOfficeType())
                .eq(SysOfficeCollect::getType, SysOfficeConstant.ARRIVALS_OR_OUTBOUND.ARRIVALS.getCode()));
        Assert.isTrue(number==0,"该工位已存在一个进站采集点");
        long outboundNumber = count(new LambdaQueryWrapper<SysOfficeCollect>()
                .eq(SysOfficeCollect::getOfficeId, dto.getOfficeId())
                .eq(SysOfficeCollect::getOfficeType, dto.getOfficeType())
                .eq(SysOfficeCollect::getType, SysOfficeConstant.ARRIVALS_OR_OUTBOUND.OUTBOUND.getCode()));
        Assert.isTrue(outboundNumber==0,"该工位已存在一个出站采集点");
        SysOfficeCollect sysOfficeCollect = new SysOfficeCollect();
        BeanUtils.copyProperties(dto,sysOfficeCollect);
        return save(sysOfficeCollect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(SysOfficeCollectUpdateDTO dto) {
        SysOfficeCollect sysOfficeCollect = new SysOfficeCollect();
        BeanUtils.copyProperties(dto,sysOfficeCollect);
        return updateById(sysOfficeCollect);
    }

    @Override
    public List<SysOfficeCollectVO> listOnlyStation(Integer type) {
        List<SysOfficeCollectVO> resList = Lists.newArrayList();
        if (Objects.nonNull(type)) {
            List<SysOfficeCollect> dbList = list(new LambdaQueryWrapper<SysOfficeCollect>()
                    .eq(SysOfficeCollect::getOfficeType, 13)
                    .eq(SysOfficeCollect::getType, type));
            if (CollUtil.isNotEmpty(dbList)) {
                resList = BeanUtil.copyToList(dbList, SysOfficeCollectVO.class);
            }
        }
        return resList;
    }

    @Override
    public SysOfficeCollectVO outCollect(Integer stationId) {
        SysOfficeCollectVO res = null;
        SysOfficeCollect one = getOne(new LambdaQueryWrapper<SysOfficeCollect>()
                .eq(SysOfficeCollect::getOfficeId, stationId)
                .eq(SysOfficeCollect::getOfficeType, 13)
                .eq(SysOfficeCollect::getType, 1), false);
        if (Objects.nonNull(one)) {
            res = new SysOfficeCollectVO();
            BeanUtils.copyProperties(one, res);
        }
        return res;
    }

    @Override
    public Map<Integer, String> listByStation(Set<Integer> stationIdSet, Integer type) {
        Map<Integer, String> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(stationIdSet)) {
            List<SysOfficeCollect> dbList = list(new LambdaQueryWrapper<SysOfficeCollect>()
                    .eq(SysOfficeCollect::getOfficeType, 13)
                    .in(SysOfficeCollect::getOfficeId,stationIdSet)
                    .eq(SysOfficeCollect::getType, type));
            if (CollUtil.isNotEmpty(dbList)) {
               dbList.forEach( collect ->{
                   resMap.put(collect.getOfficeId(),collect.getCode());
               });
            }
        }
        return resMap;
    }
}




