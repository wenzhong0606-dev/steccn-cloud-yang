package com.steccn.ems.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.dto.EpDayReportCountRowVO;
import com.steccn.ems.dto.EpDayReportCountVO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.service.EpDayReportService;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadDistantService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Override
    public EpDayReportCountVO count(LocalDate start) {
        EpDayReportCountVO resVo = new EpDayReportCountVO();
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

        Optional<EpEnergyTypeVO> oilOp = energyTypeList.stream().filter(v -> v.getId() == 4).findFirst();
        //获取用油量数据
        List<EpDayReportCountRowVO> oilRowList = consumeRow(oilOp.get(), start, end, Lists.newArrayList(new TupleDTO<>(23L, "新能源"), new TupleDTO<>(24L, "基片")), allThickList);

        //获取用气量
        Optional<EpEnergyTypeVO> gasOp = energyTypeList.stream().filter(v -> v.getId() == 3).findFirst();
        List<EpDayReportCountRowVO> gasRowList = consumeRow(gasOp.get(), start, end, Lists.newArrayList(new TupleDTO<>(12L, "新能源"), new TupleDTO<>(13L, "基片")), allThickList);

        //获取用气量
        Optional<EpEnergyTypeVO> waterOp = energyTypeList.stream().filter(v -> v.getId() == 2).findFirst();
        List<EpDayReportCountRowVO> waterRowList = consumeRow(waterOp.get(), start, end, Lists.newArrayList(new TupleDTO<>(45L, "新能源"), new TupleDTO<>(46L, "基片")), allThickList);

        //获取指定的组织结构名称和主键


        List<EpDayReportCountRowVO> dataList = Lists.newArrayList();
        dataList.addAll(waterRowList);
        dataList.addAll(gasRowList);
        dataList.addAll(oilRowList);
        resVo.setDataList(dataList);
        return resVo;
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

        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        //如果部门不止一个 ，需要构造一个合计
        List<BigDecimal> sumList = Lists.newArrayList();
        BigDecimal total = null;
        int count;

        for (int i = 0; i < partitionList.size(); i++) {
            count = 0;
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<String> consumeList = Lists.newArrayList();
            BigDecimal sum = null;
            for (int j = i; j < data.size(); j += partitionIdList.size()) {
                BigDecimal ele = data.get(j);
                consumeList.add(BigDecimalUtil.format(ele));
                //列合计
                if (count > sumList.size() - 1) {
                    sumList.add(count, BigDecimal.ZERO);
                }
                sumList.set(count, BigDecimalUtil.add(sumList.get(count), ele));
                count++;
                sum = BigDecimalUtil.add(sum, ele);
            }
            row.setConsumeList(consumeList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName(partitionList.get(i).getField2());
            row.setSumStr(BigDecimalUtil.format(sum));
            rowList.add(row);
            //合计行总计
            total = BigDecimalUtil.add(total, sum);

        }

        //如果超过一行，则添加一个合计行
        if (partitionIdList.size() > 1) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<String> sumStrList = Lists.newArrayList();
            sumList.forEach(ele -> sumStrList.add(BigDecimalUtil.format(ele)));
            row.setConsumeList(sumStrList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName("合计");
            row.setSumStr(BigDecimalUtil.format(total));
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
    private EpDayReportCountRowVO countHead(LocalDate start, LocalDate end) {
        //获取一个月的所有日期
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(start, end);
        EpDayReportCountRowVO head = new EpDayReportCountRowVO();
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
