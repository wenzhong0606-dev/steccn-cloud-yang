package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.DpConstant;
import com.steccn.dp.domain.TbBusiMonitor;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.mapper.TbBusiMonitorMapper;
import com.steccn.dp.service.TbBusiMonitorService;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteOfficeFeign;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【tb_busi_monitor】的数据库操作Service实现
 * @createDate 2024-10-21 16:20:05
 */
@Service
@RequiredArgsConstructor
public class TbBusiMonitorServiceImpl extends ServiceImpl<TbBusiMonitorMapper, TbBusiMonitor>
        implements TbBusiMonitorService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final OaInfoService oaInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveList(List<DpBusiMonitorDTO> dataList) {
        //一次只能保存一个category和code
        List<String> alarmCodeList = dataList.stream().map(DpBusiMonitorDTO::getAlarmCode).distinct().collect(Collectors.toList());
        Assert.isTrue(alarmCodeList.size() == 1, "一次只能插入一种告警类型");
        DpConstant.BusiMonitorAlarmEnum alarmCodeEnum = DpConstant.BusiMonitorAlarmEnum.getByCode(alarmCodeList.get(0));
        Assert.notNull(alarmCodeEnum, "告警类型不存在");
        Set<Integer> existIdSet = dataList.stream().filter(v -> Objects.nonNull(v.getId())).map(DpBusiMonitorDTO::getId).collect(Collectors.toSet());
        //先删除不存在的
        remove(new LambdaQueryWrapper<TbBusiMonitor>()
                .in(TbBusiMonitor::getCategory, alarmCodeEnum.getCategory().getCode())
                .in(TbBusiMonitor::getAlarmCode, alarmCodeEnum.toString())
                .notIn(CollUtil.isNotEmpty(existIdSet), TbBusiMonitor::getId, existIdSet));
        //更新已存在的
        List<DpBusiMonitorDTO> updateList = dataList.stream().filter(v -> Objects.nonNull(v.getId())).collect(Collectors.toList());
        List<TbBusiMonitor> updateBatchList = Lists.newArrayList();
        updateList.forEach(ele -> {
            TbBusiMonitor tbBusiMonitor = new TbBusiMonitor();
            BeanUtil.copyProperties(ele, tbBusiMonitor);
            tbBusiMonitor.setCategory(alarmCodeEnum.getCategory().getCode());
            tbBusiMonitor.setAlarmCode(alarmCodeEnum.toString());
            updateBatchList.add(tbBusiMonitor);
        });
        if (CollUtil.isNotEmpty(updateBatchList)) {
            updateBatchById(updateBatchList);
        }
        //插入新的
        List<DpBusiMonitorDTO> insertList = dataList.stream().filter(v -> Objects.isNull(v.getId())).collect(Collectors.toList());
        List<TbBusiMonitor> insertBatchList = Lists.newArrayList();
        insertList.forEach(ele -> {
            TbBusiMonitor tbBusiMonitor = new TbBusiMonitor();
            BeanUtil.copyProperties(ele, tbBusiMonitor);
            tbBusiMonitor.setCategory(alarmCodeEnum.getCategory().getCode());
            tbBusiMonitor.setAlarmCode(alarmCodeEnum.toString());
            insertBatchList.add(tbBusiMonitor);
        });
        if (CollUtil.isNotEmpty(insertBatchList)) {
            saveBatch(insertBatchList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>> showAll(List<String> alarmCodeList) {
        //根据code获取枚举
        Set<DpConstant.BusiMonitorAlarmEnum> alarmEnumSet = Sets.newHashSet();
        Set<DpConstant.BusiMonitorCategoryEnum> categoryEnumSet = Sets.newHashSet();
        alarmCodeList.forEach(ele -> {
            DpConstant.BusiMonitorAlarmEnum alarm = DpConstant.BusiMonitorAlarmEnum.getByCode(ele);
            Assert.notNull(alarm, "告警编码不存在");
            alarmEnumSet.add(alarm);
            categoryEnumSet.add(alarm.getCategory());
        });
        List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>> resList = Lists.newArrayList();
        List<TbBusiMonitor> dbList = list();
        //获取回显的用户名称和监控范围名称
        Set<Integer> officeIdSet = dbList.stream().map(TbBusiMonitor::getMonitorRange).filter(ele -> CollUtil.isNotEmpty(ele)).flatMap(Collection::stream).collect(Collectors.toSet());
        List<Long> manIdList = dbList.stream().map(TbBusiMonitor::getNotifyMan).filter(ele -> CollUtil.isNotEmpty(ele)).flatMap(Collection::stream).map(ele -> ele.longValue()).distinct().collect(Collectors.toList());
        Map<Integer, String> officeIdNameMap = Maps.newHashMap();
        Map<Long, String> manIdNameMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(officeIdSet)) {
            officeIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        }
        if (CollUtil.isNotEmpty(manIdList)) {
            manIdNameMap = FeignOptional.of(() -> oaInfoService.listByIdCh(manIdList), FeignConstant.OA_SERVICE_NAME).get();
        }
        //将dbList 转换为自己的dto
        List<DpBusiMonitorDTO> dtoList = BeanUtil.copyToList(dbList, DpBusiMonitorDTO.class);
        for (DpBusiMonitorDTO busiMonitorDTO : dtoList) {
            if (CollUtil.isNotEmpty(busiMonitorDTO.getMonitorRange())) {
                List<TupleDTO<Integer, String>> monitorRangeIdNameList = Lists.newArrayList();
                for (Integer ele : busiMonitorDTO.getMonitorRange()) {
                    monitorRangeIdNameList.add(new TupleDTO<>(ele, officeIdNameMap.get(ele)));
                }
                busiMonitorDTO.setMonitorRangeIdNameList(monitorRangeIdNameList);
            }
            List<TupleDTO<Long, String>> notifyManIdNameList = Lists.newArrayList();
            for (Long ele : busiMonitorDTO.getNotifyMan()) {
                notifyManIdNameList.add(new TupleDTO<>(ele, manIdNameMap.get(ele)));
            }
            busiMonitorDTO.setNotifyManIdNameList(notifyManIdNameList);
        }

        Map<Integer, List<DpBusiMonitorDTO>> categoryMap = dtoList.stream().collect(Collectors.groupingBy(DpBusiMonitorDTO::getCategory));
        for (DpConstant.BusiMonitorCategoryEnum categoryEnum : DpConstant.BusiMonitorCategoryEnum.values()) {
            List<DpBusiMonitorDTO> categoryList = categoryMap.get(categoryEnum.getCode());
            if (CollUtil.isNotEmpty(categoryList) && categoryEnumSet.contains(categoryEnum)) {
                TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>> firstTupleDTO = new TupleDTO<>();
                resList.add(firstTupleDTO);
                firstTupleDTO.setField1(categoryEnum.getCode());
                List<TupleDTO<String, List<DpBusiMonitorDTO>>> field2List = Lists.newArrayList();
                firstTupleDTO.setField2(field2List);
                Map<String, List<DpBusiMonitorDTO>> alarmMap = categoryList.stream().collect(Collectors.groupingBy(DpBusiMonitorDTO::getAlarmCode));
                for (DpConstant.BusiMonitorAlarmEnum alarmEnum : DpConstant.BusiMonitorAlarmEnum.getByCategory(categoryEnum)) {
                    List<DpBusiMonitorDTO> alarmList = alarmMap.get(alarmEnum.toString());
                    if (CollUtil.isNotEmpty(alarmList) && alarmEnumSet.contains(alarmEnum)) {
                        TupleDTO<String, List<DpBusiMonitorDTO>> thirdTupleDTO = new TupleDTO<>();
                        thirdTupleDTO.setField1(alarmEnum.toString());
                        thirdTupleDTO.setField2(alarmList);
                        field2List.add(thirdTupleDTO);
                    }
                }
            }
        }
        return resList;
    }

    @Override
    public List<DpBusiMonitorDTO> getBusiMonitorAlarm(String alarmCode) {
        List<DpBusiMonitorDTO> resList = Lists.newArrayList();
        List<TbBusiMonitor> dbList = list(new LambdaQueryWrapper<TbBusiMonitor>().eq(TbBusiMonitor::getAlarmCode, alarmCode));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                DpBusiMonitorDTO res = new DpBusiMonitorDTO();
                BeanUtil.copyProperties(ele, res);
                resList.add(res);
            });
        }
        return resList;
    }
}




