package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.MesEqpInsTask;
import com.steccn.mes.domain.MesEqpInsTaskEqpItem;
import com.steccn.mes.service.MesEqpInsTaskEqpItemService;
import com.steccn.mes.mapper.MesEqpInsTaskEqpItemMapper;
import com.steccn.mes.service.MesEqpInsTaskService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【mes_eqp_ins_task_eqp_item(点检路线设备点检项关联表)】的数据库操作Service实现
* @createDate 2024-12-26 14:28:51
*/
@Service
@RequiredArgsConstructor
public class MesEqpInsTaskEqpItemServiceImpl extends ServiceImpl<MesEqpInsTaskEqpItemMapper, MesEqpInsTaskEqpItem>
    implements MesEqpInsTaskEqpItemService{
    private final MesEqpInsTaskService mesEqpInsTaskService;

    @Override
    public List<TupleDTO<LocalDateTime, String>> paoPosition(LocalDate day) {
        List<TupleDTO<LocalDateTime, String>> results = Lists.newArrayList();
        List<MesEqpInsTask> tasks = mesEqpInsTaskService.list(new LambdaQueryWrapper<MesEqpInsTask>()
                .select(MesEqpInsTask::getId, MesEqpInsTask::getPlanTime)
                .in(MesEqpInsTask::getStatus, Sets.newHashSet(0,4))
                .ge(MesEqpInsTask::getPlanTime, day.atTime(LocalTime.of(8, 0, 0)))
                .lt(MesEqpInsTask::getPlanTime, day.plusDays(1).atTime(LocalTime.of(8, 0, 0))));
        Set<Long> taskId = tasks.stream().map(MesEqpInsTask::getId).collect(Collectors.toSet());
        List<MesEqpInsTaskEqpItem> insItemValue = list(new LambdaQueryWrapper<MesEqpInsTaskEqpItem>()
                .select(MesEqpInsTaskEqpItem::getInsTaskId, MesEqpInsTaskEqpItem::getItemValue)
                .in(MesEqpInsTaskEqpItem::getInsTaskId, taskId)
                .eq(MesEqpInsTaskEqpItem::getInsItemId, 1685104579857432576L));
        if(CollUtil.isNotEmpty(insItemValue)){
            insItemValue.forEach(tmp->{
                if(StrUtil.isNotBlank(tmp.getItemValue())){
                    TupleDTO<LocalDateTime, String> tup = new TupleDTO<>();
                    tup.setField1(tasks.stream().filter(v->v.getId().equals(tmp.getInsTaskId())).findFirst().orElse(null).getPlanTime());
                    tup.setField2(tmp.getItemValue());
                    results.add(tup);
                }
            });
        }
        return results;
    }
}




