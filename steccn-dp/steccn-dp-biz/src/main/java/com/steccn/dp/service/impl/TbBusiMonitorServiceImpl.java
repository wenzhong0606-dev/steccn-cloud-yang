package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.DpConstant;
import com.steccn.dp.domain.TbBusiMonitor;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.service.TbBusiMonitorService;
import com.steccn.dp.mapper.TbBusiMonitorMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【tb_busi_monitor】的数据库操作Service实现
 * @createDate 2024-10-21 16:20:05
 */
@Service
public class TbBusiMonitorServiceImpl extends ServiceImpl<TbBusiMonitorMapper, TbBusiMonitor>
        implements TbBusiMonitorService {

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
    public List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>> showAll() {
        List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>> resList = Lists.newArrayList();
        List<TbBusiMonitor> dbList = list();
        Map<Integer, List<TbBusiMonitor>> categoryMap = dbList.stream().collect(Collectors.groupingBy(TbBusiMonitor::getCategory));
        for (DpConstant.BusiMonitorCategoryEnum categoryEnum : DpConstant.BusiMonitorCategoryEnum.values()) {
            List<TbBusiMonitor> categoryList = categoryMap.get(categoryEnum.getCode());
            if (CollUtil.isNotEmpty(categoryList)) {
                TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>> firstTupleDTO = new TupleDTO<>();
                resList.add(firstTupleDTO);
                firstTupleDTO.setField1(categoryEnum.getCode());
                List<TupleDTO<String, List<DpBusiMonitorDTO>>> field2List = Lists.newArrayList();
                firstTupleDTO.setField2(field2List);
                Map<String, List<TbBusiMonitor>> alarmMap = categoryList.stream().collect(Collectors.groupingBy(TbBusiMonitor::getAlarmCode));
                for (DpConstant.BusiMonitorAlarmEnum alarmEnum : DpConstant.BusiMonitorAlarmEnum.getByCategory(categoryEnum)) {
                    List<TbBusiMonitor> alarmList = alarmMap.get(alarmEnum.toString());
                    if (CollUtil.isNotEmpty(alarmList)) {
                        List<DpBusiMonitorDTO> alarmCopyList = BeanUtil.copyToList(alarmList, DpBusiMonitorDTO.class);
                        TupleDTO<String, List<DpBusiMonitorDTO>> thirdTupleDTO = new TupleDTO<>();
                        thirdTupleDTO.setField1(alarmEnum.toString());
                        thirdTupleDTO.setField2(alarmCopyList);
                        field2List.add(thirdTupleDTO);
                    }
                }
            }
        }
        return resList;
    }
}




