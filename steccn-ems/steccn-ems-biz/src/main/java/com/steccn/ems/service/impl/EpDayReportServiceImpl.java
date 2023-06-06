package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc
 * @date 2023/6/1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDayReportServiceImpl implements EpDayReportService {

    private final EpConsumePartitionChartService epConsumePartitionChartService;
    private final EpEnergyTypeService epEnergyTypeService;
    private final EpMeterReadDistantService epMeterReadDistantService;
    private final EpPartitionService epPartitionService;
    private final EpHeatPowerService epHeatPowerService;
    private final EpMeterReadService meterReadService;

    @Override
    public EpDayReportCountVO count(LocalDate start) {

        EpDayReportCountVO resVo = new EpDayReportCountVO();
        List<EpDayReportCountRowVO> dataList = Lists.newArrayList();
        //获取一个月最后一天--如果当前日期大于end，则只显示到end，否则至显示到当前日期
        LocalDateTime endLdt = LocalDateUtil.getLastDayOfMonthStr(start);
        LocalDate end = endLdt.toLocalDate();
        LocalDate now = LocalDate.now();
        if (end.isAfter(now)) {
            end = now;
        }
        //获取表头
        resVo.setHead(countHead(start, end));
        //获取所有的能源类型
        List<EpEnergyTypeVO> energyTypeList = epEnergyTypeService.getEpEnergyTypeVOList();

        //获取全部厚度
        List<BigDecimal> allThickList = epMeterReadDistantService.selectAllThick(start, end);

        //获取指定的组织结构名称和主键
        Map<Integer, List<TupleDTO<Long, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();

        //计算电耗
        Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC.equals(v.getId())).findFirst();
        List<EpDayReportCountRowVO> elecList = calElec(eleOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC), allThickList);
        dataList.addAll(elecList);

        //计算余热发电
        List<BigDecimal> heatPowerList = epHeatPowerService.heatPower(start, end);
        //获取能源类型
        EpEnergyTypeVO eleEnergy = eleOp.get();
        EpDayReportCountRowVO heatPowerRow = new EpDayReportCountRowVO();
        heatPowerList.forEach(ele -> ele.setScale(0));
        heatPowerRow.setConsumeList(heatPowerList);
        heatPowerRow.setEnergyType(eleEnergy.getName());
        heatPowerRow.setDepartmentName("余热发电");
        heatPowerRow.setUnit(eleEnergy.getMeasureUnitName());
        BigDecimal reduce = heatPowerList.stream().filter(v -> Objects.nonNull(v)).reduce(BigDecimal.ZERO, BigDecimal::add);
        heatPowerRow.setSumStr(BigDecimalUtil.setScale(reduce, 0));
        dataList.add(heatPowerRow);

        try {
            //获取用油量数据
            Optional<EpEnergyTypeVO> oilOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_OIL.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> oilRowList = consumeRow(oilOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_OIL), allThickList);
            dataList.addAll(oilRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        try {
            //获取用气量
            Optional<EpEnergyTypeVO> gasOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_GAS.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> gasRowList = consumeRow(gasOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_GAS), allThickList);
            dataList.addAll(gasRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        try {
            //获取用水量
            Optional<EpEnergyTypeVO> waterOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_WATER.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> waterRowList = consumeRow(waterOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_WATER), allThickList);
            dataList.addAll(waterRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }

        resVo.setDataList(dataList);
        return resVo;
    }

    /**
     * 计算电
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 所有节点的集合
     * @param thickList     所有厚度集合
     * @return
     */
    private List<EpDayReportCountRowVO> calElec(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TupleDTO<Long, String>> partitionList, List<BigDecimal> thickList) {
        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);
        //每个节点，每天的数据
        //基片制造部
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> v.getOfficeName().equals("基片制造部")).collect(Collectors.toList());
        List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());

        //构建基片制造部
        EpDayReportCountRowVO jpRow = new EpDayReportCountRowVO();
        BigDecimal jpSum = BigDecimal.ZERO;
        List<BigDecimal> jpValueList = Lists.newArrayList();
        for (LocalDate ele : timeList) {
            List<EpMeterReadAllDataVO> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
            BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
            jpSum = BigDecimalUtil.add(jpSum, reduce);
            jpValueList.add(reduce.setScale(0));
        }
        jpRow.setSumStr(jpSum.setScale(0));
        jpRow.setConsumeList(jpValueList);
        jpRow.setUnit(epEnergy.getMeasureUnitName());
        jpRow.setEnergyType(epEnergy.getName());
        jpRow.setDepartmentName("基片制造部");
        return Lists.newArrayList(jpRow);
    }

    /**
     * 获取能耗行
     *
     * @param energyType
     * @param start
     * @param end
     * @param partitionList
     * @return
     */
    private List<EpDayReportCountRowVO> consumeRow(EpEnergyTypeVO energyType, LocalDate start, LocalDate end, List<TupleDTO<Long, String>> partitionList, List<BigDecimal> allThickList) {
        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        if (CollUtil.isEmpty(partitionList) || Objects.isNull(energyType)) {
            return rowList;
        }
        EpTableDTO epTableDTO = new EpTableDTO();
        epTableDTO.setEnergyType(energyType.getId());
        epTableDTO.setStart(start);
        epTableDTO.setEnd(end);
        epTableDTO.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        epTableDTO.setPartitionIdList(partitionIdList);
        epTableDTO.setThick(allThickList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> epChart = epConsumePartitionChartService.consumePartitionContrast(epTableDTO);
        List<BigDecimal> data = epChart.getSeriesDataVO().get(0).getData();

        //不同厚度的数据要汇总到一起
        for (int i = 1; i < epChart.getSeriesDataVO().size(); i++) {
            List<BigDecimal> temp = epChart.getSeriesDataVO().get(i).getData();
            for (int j = 0; j < temp.size(); j++) {
                BigDecimal oldValue = data.get(j);
                oldValue = BigDecimalUtil.add(oldValue, temp.get(j));
                data.set(j, oldValue);
            }
        }

        //如果部门不止一个 ，需要构造一个合计
        List<BigDecimal> sumList = Lists.newArrayList();
        BigDecimal total = null;
        int count;

        for (int i = 0; i < partitionList.size(); i++) {
            count = 0;
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> consumeList = Lists.newArrayList();
            BigDecimal sum = null;
            for (int j = i; j < data.size(); j += partitionIdList.size()) {
                BigDecimal ele = data.get(j);
                consumeList.add(ele);
                //列合计
                if (count > sumList.size() - 1) {
                    sumList.add(count, BigDecimal.ZERO);
                }
                sumList.set(count, BigDecimalUtil.add(sumList.get(count), ele));
                count++;
                sum = BigDecimalUtil.add(sum, ele);
            }
            consumeList.forEach(ele -> ele = ele.setScale(0));
            row.setConsumeList(consumeList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName(partitionList.get(i).getField2());
            row.setSumStr(sum.setScale(0));
            rowList.add(row);
            //合计行总计
            total = BigDecimalUtil.add(total, sum);

        }

        //如果超过一行，则添加一个合计行
        if (partitionIdList.size() > 1) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> sumStrList = Lists.newArrayList();
            sumList.forEach(ele -> {
                ele = ele.setScale(0);
                sumStrList.add(ele);
            });
            row.setConsumeList(sumStrList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName("合计");
            row.setSumStr(total.setScale(0));
            rowList.add(row);
        }

        return rowList;
    }

    /**
     * 获取表头
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    private EpDayReportCountHeadVO countHead(LocalDate start, LocalDate end) {
        //获取一个月的所有日期
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(start, end);
        EpDayReportCountHeadVO head = new EpDayReportCountHeadVO();
        head.setEnergyType("能源类型");
        head.setUnit("单位");
        head.setDepartmentName("部门");
        List<String> dateStrList = Lists.newArrayList();
        dateList.forEach(ele -> dateStrList.add(LocalDateTimeUtil.format(ele, "MM月dd日")));
        head.setConsumeList(dateStrList);
        head.setSumStr("总计");
        return head;
    }
}
