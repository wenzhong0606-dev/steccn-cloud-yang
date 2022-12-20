package com.steccn.ems.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
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
            yData = getDyData("燃料(气+油)", "基片制造部", "substrate", selectDate, startLocalDate, null, epOverViewVO);
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
            yData = getAlarmDate(selectDate, startLocalDate, epOverViewVO);
        }
        //获取最近7天单耗
        epOverViewVO.setYData(yData);
        //最后一条数据
        BigDecimal lastData = yData.get(yData.size() - 1);
        //第一条数据
        BigDecimal firstData = yData.get(0);
        //倒数第二条数据
        BigDecimal lastButOneData = yData.get(yData.size() - 2);
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

    @Override
    public EpSubConsumeContrastVO getSubConsumeContrast(LocalDate selectDate) {
        LocalDate startDate = selectDate.minusDays(6);
        Set<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(startDate);
        localDateSet.add(selectDate);
        List<Integer> officeIdList = new ArrayList<>();
        officeIdList.add(75);
        officeIdList.add(76);
        List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeIdList).eq(EpPartition::getType,5));
        List<Long> partitionIdList = partitionList.stream().map(EpPartition::getId).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> meterReadAllDataVOList = epMeterReadService.classDateAllDataUnit(localDateSet, 5, partitionIdList);
        List<EpMeterReadAllDataVO> readAllDataVOList = meterReadAllDataVOList.stream()
                .filter(epMeterReadAllDataVO -> partitionIdList.contains(epMeterReadAllDataVO.getOfficeId()))
                .sorted(Comparator.comparing(EpMeterReadAllDataVO::getOfficeId)).collect(Collectors.toList());

        //标准类型：部门标准
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("departmental_standard", "product_unit_consumption_standard").getData();
        //产品类型：基片
        SysDictVO productType = remoteDictFeignApi.getByCode("substrate", "product_type").getData();

        EpSubConsumeContrastVO epSubConsumeContrastVO = new EpSubConsumeContrastVO();
        //获取x轴数据
        List<String> xData = partitionList.stream().map(EpPartition::getOfficeName).collect(Collectors.toList());
        epSubConsumeContrastVO.setXAxis(xData);
        //获取y轴数据
        LinkedHashMap<Long, List<EpMeterReadAllDataVO>> map = readAllDataVOList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getOfficeId, LinkedHashMap::new, Collectors.toList()));
        List<EpSubConsumeDataVO> epSubConsumeDataVOList = new ArrayList<>();
        epSubConsumeContrastVO.setYData(epSubConsumeDataVOList);
        for (int i = 0; i < 2; i++) {
            EpSubConsumeDataVO epSubConsumeDataVO = new EpSubConsumeDataVO();
            if (Objects.equals(i,0)) {
                epSubConsumeDataVO.setName("实际能耗");
            } else {
                epSubConsumeDataVO.setName("部门标准");
            }
            List<BigDecimal> dataList = new ArrayList<>();
            epSubConsumeDataVO.setDataList(dataList);
            epSubConsumeDataVOList.add(epSubConsumeDataVO);

            for (EpPartition epPartition : partitionList) {
                if (Objects.equals(i,0)) {
                    List<EpMeterReadAllDataVO> epMeterReadAllDataVOList = map.get(epPartition.getId());
                    //获取单耗值
                    dataList.add(getUnitValue(epMeterReadAllDataVOList));
                } else {
                    //获取部门单耗标准
                    LambdaQueryWrapper<EpProductUnitConsumeStandard> queryWrapper = new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                            .eq(EpProductUnitConsumeStandard::getEnergyType, 5)
                            .gt(EpProductUnitConsumeStandard::getValidStart, startDate).lt(EpProductUnitConsumeStandard::getValidEnd, selectDate)
                            .eq(EpProductUnitConsumeStandard::getStandardType, sysDictVO.getId())
                            .eq(EpProductUnitConsumeStandard::getProductType, productType.getId())
                            .like(EpProductUnitConsumeStandard::getApplyOffice,epPartition.getId());
                    //部门单耗标准
                    EpProductUnitConsumeStandard productUnitConsumeStandard = consumeStandardService.getOne(queryWrapper);
                    dataList.add(productUnitConsumeStandard.getUnitValue());
                }
            }
        }
        return epSubConsumeContrastVO;
    }

    @Override
    public EpDeepConsumeContrastVO getDeepConsumeContrast(LocalDate selectDate, Integer type) {
        LocalDate startDate = selectDate.minusDays(6);
        Set<LocalDate> localDateSet = new HashSet<>();
        localDateSet.add(startDate);
        localDateSet.add(selectDate);
        List<Integer> officeIdList = new ArrayList<>();
        officeIdList.add(79);
        officeIdList.add(78);

        BigDecimal thick;
        if (type.equals(0)) {
            thick = BigDecimal.valueOf(2);
        } else {
            thick = BigDecimal.valueOf(3.2);
        }

        List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getOfficeId, officeIdList).eq(EpPartition::getType,1));
        Set<Long> partitionIdSet = partitionList.stream().map(EpPartition::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(partitionIdSet)) {
            partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getParentId, partitionIdSet).eq(EpPartition::getType, 1));
        }
        List<Long> partitionIdList = partitionList.stream().map(EpPartition::getId).collect(Collectors.toList());

        List<EpMeterReadAllDataVO> meterReadAllDataVOList = epMeterReadService.classDateAllDataUnit(localDateSet, 1, partitionIdList);
        List<EpMeterReadAllDataVO> readAllDataVOList = meterReadAllDataVOList.stream()
                .filter(epMeterReadAllDataVO -> partitionIdList.contains(epMeterReadAllDataVO.getOfficeId()) && epMeterReadAllDataVO.getThick().compareTo(thick) == 0)
                .sorted(Comparator.comparing(EpMeterReadAllDataVO::getOfficeId)).collect(Collectors.toList());

        //标准类型：企业标准
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("company_standard", "product_unit_consumption_standard").getData();
        //产品类型：深加工
        SysDictVO productType = remoteDictFeignApi.getByCode("deep_processing", "product_type").getData();
        LambdaQueryWrapper<EpProductUnitConsumeStandard> queryWrapper = new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getEnergyType, 1)
                .gt(EpProductUnitConsumeStandard::getValidStart, selectDate).lt(EpProductUnitConsumeStandard::getValidEnd, selectDate)
                .eq(EpProductUnitConsumeStandard::getStandardType, sysDictVO.getId())
                .eq(EpProductUnitConsumeStandard::getProductType, productType.getId())
                .eq(EpProductUnitConsumeStandard::getProductThick, thick);
        EpProductUnitConsumeStandard productUnitConsumeStandard = consumeStandardService.getOne(queryWrapper);

        EpDeepConsumeContrastVO epDeepConsumeContrastVO = new EpDeepConsumeContrastVO();
        //获取标准值
        if (Objects.nonNull(productUnitConsumeStandard)) {
            epDeepConsumeContrastVO.setStandardValue(productUnitConsumeStandard.getUnitValue());
        }
        //获取x轴
        List<String> officeNameList = partitionList.stream().map(EpPartition::getOfficeName).collect(Collectors.toList());
        epDeepConsumeContrastVO.setXAxis(officeNameList);
        //获取y轴数据
        LinkedHashMap<Long, List<EpMeterReadAllDataVO>> map = readAllDataVOList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getOfficeId, LinkedHashMap::new, Collectors.toList()));
        List<BigDecimal> dataList = new ArrayList<>();
        epDeepConsumeContrastVO.setYData(dataList);
        for (EpPartition epPartition : partitionList) {
            List<EpMeterReadAllDataVO> epMeterReadAllDataVOList = map.get(epPartition.getId());
            if (CollectionUtils.isNotEmpty(epMeterReadAllDataVOList)) {
                dataList.add(getUnitValue(epMeterReadAllDataVOList));
            } else {
                dataList.add(BigDecimal.ZERO);
            }
        }

        return epDeepConsumeContrastVO;
    }

    private BigDecimal getUnitValue(List<EpMeterReadAllDataVO> epMeterReadAllDataVOList) {
        //汇总能耗值
        BigDecimal sumConsume = epMeterReadAllDataVOList.stream().filter(meterReadAllDataVO -> Objects.nonNull(meterReadAllDataVO.getConsume()))
                .map(EpMeterReadAllDataVO::getConsume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //汇总生产值
        BigDecimal sumProductNum = epMeterReadAllDataVOList.stream().filter(meterReadAllDataVO -> Objects.nonNull(meterReadAllDataVO.getProductNum()))
                .map(EpMeterReadAllDataVO::getProductNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //获取单耗值
        return BigDecimalUtil.divide(sumConsume,sumProductNum);
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
        if (Objects.nonNull(productUnitConsumeStandard)) {
            epOverViewVO.setTargetValue(productUnitConsumeStandard.getUnitValue());
        }
        for (LocalDate localDate : dxData) {
            List<EpMeterReadAllDataVO> readAllDataVOList = meterReadAllDataVOList.stream().filter(epMeterReadAllDataVO -> localDate.isEqual(epMeterReadAllDataVO.getReadDate())).collect(Collectors.toList());
            BigDecimal sumConsume = readAllDataVOList.stream().map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumProductNum = readAllDataVOList.stream().map(EpMeterReadAllDataVO::getProductNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            dateConsumeList.add(BigDecimalUtil.divide(sumConsume, sumProductNum));
        }
        return dateConsumeList;
    }

}
