package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.DpConstant;
import com.steccn.dp.domain.TbBusiMonitor;
import com.steccn.dp.dto.DpBusiMonitorSaveDTO;
import com.steccn.dp.service.TbBusiMonitorService;
import com.steccn.dp.mapper.TbBusiMonitorMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public Boolean saveList(List<DpBusiMonitorSaveDTO> dataList) {
        //一次只能保存一个category和code
        List<String> alarmCodeList = dataList.stream().map(DpBusiMonitorSaveDTO::getAlarmCode).distinct().collect(Collectors.toList());
        Assert.isTrue(alarmCodeList.size() == 1, "一次只能插入一种告警类型");
        DpConstant.BusiMonitorAlarmEnum alarmCodeEnum = DpConstant.BusiMonitorAlarmEnum.getByCode(alarmCodeList.get(0));
        Assert.notNull(alarmCodeEnum, "告警类型不存在");
        Set<Integer> existIdSet = dataList.stream().filter(v -> Objects.nonNull(v.getId())).map(DpBusiMonitorSaveDTO::getId).collect(Collectors.toSet());
        //先删除不存在的
        remove(new LambdaQueryWrapper<TbBusiMonitor>()
                .in(TbBusiMonitor::getCategory, alarmCodeEnum.getCategory().getCode())
                .in(TbBusiMonitor::getAlarmCode, alarmCodeEnum.toString())
                .notIn(CollUtil.isNotEmpty(existIdSet), TbBusiMonitor::getId, existIdSet));
        //更新已存在的
        List<DpBusiMonitorSaveDTO> updateList = dataList.stream().filter(v -> Objects.nonNull(v.getId())).collect(Collectors.toList());
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
        List<DpBusiMonitorSaveDTO> insertList = dataList.stream().filter(v -> Objects.isNull(v.getId())).collect(Collectors.toList());
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
}




