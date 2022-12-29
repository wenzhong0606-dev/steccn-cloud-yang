package com.steccn.ems.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.steccn.ems.dto.EpUnitConsumeAlarmDTO;
import com.steccn.ems.mapper.EpUnitConsumeAlarmMapper;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.EpUnitConsumeAlarmService;
import com.steccn.ems.vo.EpUnitConsumeAlarmVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_unit_consume_alarm(单耗告警表)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@RequiredArgsConstructor
@Service
public class EpUnitConsumeAlarmServiceImpl extends ServiceImpl<EpUnitConsumeAlarmMapper, EpUnitConsumeAlarm>
    implements EpUnitConsumeAlarmService{

    private final EpEnergyTypeService epEnergyTypeService;

    private final EpPartitionService epPartitionService;

    @Override
    public Page<EpUnitConsumeAlarmVO> page(EpUnitConsumeAlarmDTO dto) {
        LambdaQueryWrapper<EpUnitConsumeAlarm> condition = new LambdaQueryWrapper<EpUnitConsumeAlarm>();

        if (Objects.nonNull(dto.getEnergyType())) {
            condition.eq(EpUnitConsumeAlarm::getEnergyType,dto.getEnergyType());
        }
        if (Objects.nonNull(dto.getPartitionId())) {
            if (Objects.nonNull(dto.getEnergyType())) {
                condition.eq(EpUnitConsumeAlarm::getOfficeId,dto.getPartitionId());
            } else {
                List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getPartitionId()));
                Set<Long> partitionIdSet = partitionList.stream().map(EpPartition::getId).collect(Collectors.toSet());
                condition.in(EpUnitConsumeAlarm::getOfficeId,partitionIdSet);
            }
        }
        if (Objects.nonNull(dto.getStartDate())) {
            condition.gt(EpUnitConsumeAlarm::getAlarmDate,dto.getStartDate());
        }
        if (Objects.nonNull(dto.getEndDate())) {
            condition.lt(EpUnitConsumeAlarm::getAlarmDate,dto.getEndDate());
        }
        //定义排序条件
        if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ALARM_DATE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getAlarmDate);
        }else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ENERGY_TYPE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getEnergyType);
        }else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_OFFICE_ID.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getOfficeId);
        } else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ALARM_TYPE.equals(dto.getSortField())) {
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getAlarmType);
        }

        Page<EpUnitConsumeAlarmVO> voPage = new Page<>();
        Page<EpUnitConsumeAlarm> page = page(new Page<>(dto.getCurrent(), dto.getSize()), condition);
        List<EpUnitConsumeAlarmVO> epUnitConsumeAlarmVOList = new ArrayList<>();
        List<EpPartition> list = epPartitionService.list();

        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            Set<Integer> energyTypeSet = page.getRecords().stream().map(EpUnitConsumeAlarm::getEnergyType).collect(Collectors.toSet());
            Map<Integer, String> energyTypeMap = epEnergyTypeService.getBySetIds(energyTypeSet);
            for (EpUnitConsumeAlarm epUnitConsumeAlarm : page.getRecords()) {
                EpUnitConsumeAlarmVO epUnitConsumeAlarmVO = new EpUnitConsumeAlarmVO();
                BeanUtils.copyProperties(epUnitConsumeAlarm,epUnitConsumeAlarmVO);
                epUnitConsumeAlarmVO.setEnergyTypeName(energyTypeMap.get(epUnitConsumeAlarm.getEnergyType()));

                StringBuilder sb = new StringBuilder();
                Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(epUnitConsumeAlarm.getOfficeId().longValue())).findAny();
                if (partitionOptional.isPresent()) {
                    sb.insert(0, epUnitConsumeAlarm.getOfficeName());
                    recursion(list, partitionOptional.get(), sb);
                }
                epUnitConsumeAlarmVO.setPartitionName(epUnitConsumeAlarm.getOfficeName());
                epUnitConsumeAlarmVO.setPartitionNameAll(sb.toString());
                epUnitConsumeAlarmVO.setAlarmDetail(epUnitConsumeAlarm.getContent());
                if (epUnitConsumeAlarm.getAlarmType().equals(0)){
                    epUnitConsumeAlarmVO.setAlarmType("日单耗超标");
                }
                epUnitConsumeAlarmVOList.add(epUnitConsumeAlarmVO);
            }

        }

        BeanUtils.copyProperties(page,voPage);
        voPage.setRecords(epUnitConsumeAlarmVOList);
        return voPage;
    }

    private EpPartition recursion(List<EpPartition> list, EpPartition partition, StringBuilder sb) {
        Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(partition.getParentId())).findAny();
        if (partitionOptional.isPresent()) {
            sb.insert(0, partitionOptional.get().getOfficeName() + "/");
            return recursion(list, partitionOptional.get(), sb);
        }
        return null;
    }
}




