package com.steccn.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpOverViewVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/13 13:30
 */
@Service
@RequiredArgsConstructor
public class EpOverViewServiceImpl implements EpOverViewService {

    private final EpMeterReadService epMeterReadService;

    private final EpProductUnitConsumeStandardService consumeStandardService;

    private final EpEnergyTypeService epEnergyTypeService;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final EpPartitionService epPartitionService;

    private final EpUnitConsumeAlarmService epUnitConsumeAlarmService;

    @Override
    public EpOverViewVO getOverViewByType(Integer type, LocalDate selectDate) {
        LocalDate startLocalDate = selectDate.minusDays(6);
        List<BigDecimal> yData;
        EpOverViewVO epOverViewVO = new EpOverViewVO();

        if (type.equals(EpManagerConstant.CardType.SUB_OIL_GAS.getCode())) {
            yData = getDyData("燃料", "基片制造部", "substrate", selectDate, startLocalDate, null, epOverViewVO);
        } else if (type.equals(EpManagerConstant.CardType.SUB_ELECTRIC_TWO.getCode())) {
            yData = getDyData("电",
                    "基片制造部", "substrate", selectDate, startLocalDate, BigDecimal.valueOf(2), epOverViewVO);
        } else if (type.equals(EpManagerConstant.CardType.DEEP_ELECTRIC_TWO.getCode())) {
            yData = getDyData("电",
                    "深加工制造部", "deep_processing", selectDate, startLocalDate, BigDecimal.valueOf(2), epOverViewVO);
        } else if (type.equals(EpManagerConstant.CardType.SUB_ELECTRIC_THREE.getCode())) {
            yData = getDyData("电",
                    "基片制造部", "deep_processing", selectDate, startLocalDate, BigDecimal.valueOf(3.2), epOverViewVO);
        } else if (type.equals(EpManagerConstant.CardType.DEEP_ELECTRIC_THREE.getCode())) {
            yData = getDyData("电",
                    "深加工制造部", "deep_processing", selectDate, startLocalDate, BigDecimal.valueOf(3.2), epOverViewVO);
        } else {
            yData = getAlarmDate(selectDate,startLocalDate,epOverViewVO);
        }
        //获取最近7天单耗
        epOverViewVO.setYData(yData);
        //最后一条数据
        BigDecimal lastData = yData.get(yData.size()-1);
        //第一条数据
        BigDecimal firstData = yData.get(0);
        //倒数第二条数据
        BigDecimal lastButOneData = yData.get(yData.size()-2);
        //计算日同比
        BigDecimal dayValue = BigDecimalUtil.divide(lastData.subtract(lastButOneData).abs(), lastButOneData).multiply(BigDecimalUtil.HUNDRED);
        //计算周同比
        BigDecimal weekValue = BigDecimalUtil.divide(lastData.subtract(firstData).abs(), firstData).multiply(BigDecimalUtil.HUNDRED);

        epOverViewVO.setValue(lastData);
        epOverViewVO.setDayValue(dayValue);
        epOverViewVO.setWeekValue(weekValue);
        //判断日同比上升还是下降
        if (lastData.compareTo(lastButOneData) > 0) {
            epOverViewVO.setDayFlag(1);
        } else if (lastData.compareTo(lastButOneData) < 0) {
            epOverViewVO.setDayFlag(0);
        } else {
            epOverViewVO.setDayFlag(2);
        }
        //判断周同比上升还是下降
        if (lastData.compareTo(firstData) > 0) {
            epOverViewVO.setWeekFlag(1);
        } else if (lastData.compareTo(firstData) < 0) {
            epOverViewVO.setWeekFlag(0);
        } else {
            epOverViewVO.setWeekFlag(2);
        }
        epOverViewVO.setKey(Objects.requireNonNull(EpManagerConstant.CardType.getByCode(type)).getCode());
        epOverViewVO.setTitle(Objects.requireNonNull(EpManagerConstant.CardType.getByCode(type)).getName());
        epOverViewVO.setTooltip(Objects.requireNonNull(EpManagerConstant.CardType.getByCode(type)).getTooltip());

        return epOverViewVO;
    }

    private List<BigDecimal> getAlarmDate(LocalDate selectDate, LocalDate startLocalDate, EpOverViewVO epOverViewVO) {
        LocalDate startDate = selectDate.minusDays(5);
        LocalDate endDate = selectDate.plusDays(1);
        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);
        List<LocalDate> dxData = LocalDateUtil.getLocalDateByStartEndDate(startLocalDate, selectDate);
        epOverViewVO.setXData(dxData);

        List<EpUnitConsumeAlarm> unitConsumeAlarms = epUnitConsumeAlarmService.list(new LambdaQueryWrapper<EpUnitConsumeAlarm>().gt(EpUnitConsumeAlarm::getCreateTime, startTime).le(EpUnitConsumeAlarm::getAlarmDate, endTime));
        List<BigDecimal> yData = new ArrayList<>();
        for (LocalDate localDate : dxData) {
            LocalDate plusDays = localDate.plusDays(1);
            long count = unitConsumeAlarms.stream().filter(epUnitConsumeAlarm -> plusDays.isEqual(epUnitConsumeAlarm.getCreateTime().toLocalDate())).count();
            yData.add(BigDecimal.valueOf(count));
        }
        return yData;
    }

    private List<BigDecimal> getDyData(String energyType,
                                       String partitionName,
                                       String productTypeCode,
                                       LocalDate selectDate,
                                       LocalDate startLocalDate,
                                       BigDecimal thick,
                                       EpOverViewVO epOverViewVO) {
        EpEnergyType epEnergyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getName, energyType));
        EpPartition partition = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeName, partitionName));
        //标准类型：企业标准
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("company_standard", "product_unit_consumption_standard").getData();
        //产品类型：基片
        SysDictVO productType = remoteDictFeignApi.getByCode(productTypeCode, "product_type").getData();

        LambdaQueryWrapper<EpProductUnitConsumeStandard> queryWrapper = new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getEnergyType, epEnergyType.getId())
                .gt(EpProductUnitConsumeStandard::getValidStart, selectDate).lt(EpProductUnitConsumeStandard::getValidEnd, selectDate)
                .eq(EpProductUnitConsumeStandard::getStandardType, sysDictVO.getId())
                .eq(EpProductUnitConsumeStandard::getProductType, productType.getId());
        if (Objects.nonNull(thick)) {
            queryWrapper.eq(EpProductUnitConsumeStandard::getProductThick, thick);
        }
        EpProductUnitConsumeStandard productUnitConsumeStandard = consumeStandardService.getOne(queryWrapper);

        Set<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(startLocalDate);
        localDateSet.add(selectDate);
        List<EpMeterReadAllDataVO> meterReadAllDataVOList = epMeterReadService.classDateAllDataUnit(localDateSet, epEnergyType.getId(), Collections.singletonList(partition.getId()));
        if (Objects.nonNull(thick)) {
            meterReadAllDataVOList = meterReadAllDataVOList.stream().filter(epMeterReadAllDataVO -> thick.equals(epMeterReadAllDataVO.getThick())).collect(Collectors.toList());
        }

        List<BigDecimal> dateConsumeList = new ArrayList<>();
        List<LocalDate> dxData = LocalDateUtil.getLocalDateByStartEndDate(startLocalDate, selectDate);
        //获取横坐标
        epOverViewVO.setXData(dxData);
        //获取目标值
        epOverViewVO.setTargetValue(productUnitConsumeStandard.getUnitValue());
        for (LocalDate localDate : dxData) {
            List<EpMeterReadAllDataVO> readAllDataVOList = meterReadAllDataVOList.stream().filter(epMeterReadAllDataVO -> localDate.isEqual(epMeterReadAllDataVO.getReadDate())).collect(Collectors.toList());
            BigDecimal sumConsume = readAllDataVOList.stream().map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumProductNum = readAllDataVOList.stream().map(EpMeterReadAllDataVO::getProductNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            dateConsumeList.add(BigDecimalUtil.divide(sumConsume, sumProductNum));
        }
        return dateConsumeList;
    }

}
