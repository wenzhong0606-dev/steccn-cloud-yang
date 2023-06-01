package com.steccn.ems.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.dto.EpDayReportCountRowVO;
import com.steccn.ems.dto.EpDayReportCountVO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.service.EpDayReportService;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtBaseIdNameDTO;
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
        Optional<EpEnergyTypeVO> first = energyTypeList.stream().filter(v -> v.getId() == 4).findFirst();
        //获取数据
        EpDayReportCountRowVO oilRow = consumeRow(first.get(), start, end, Lists.newArrayList(new TupleDTO<>(23L, "基片")));
        resVo.setDataList(Lists.newArrayList(oilRow));
        return resVo;
    }

    /**
     * 获取能耗行
     * @param energyType
     * @param start
     * @param end
     * @param partitionList
     * @return
     */
    private EpDayReportCountRowVO consumeRow(EpEnergyTypeVO energyType, LocalDate start, LocalDate end, List<TupleDTO<Long,String>> partitionList) {
        EpTableDTO oilEpTableDTO = new EpTableDTO();
        oilEpTableDTO.setEnergyType(energyType.getId());
        oilEpTableDTO.setStart(start);
        oilEpTableDTO.setEnd(end);
        oilEpTableDTO.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        oilEpTableDTO.setPartitionIdList(partitionIdList);
        oilEpTableDTO.setThick(Lists.newArrayList(BigDecimal.valueOf(-1.0)));
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> oilEpChart = epConsumePartitionChartService.consumePartitionContrast(oilEpTableDTO);
        List<BigDecimal> data = oilEpChart.getSeriesDataVO().get(0).getData();

        EpDayReportCountRowVO oilRow = new EpDayReportCountRowVO();
        List<String> oilConsumeList = Lists.newArrayList();
        BigDecimal sum = null;
        for (BigDecimal ele : data) {
            oilConsumeList.add(BigDecimalUtil.format(ele));
            sum = BigDecimalUtil.add(sum, ele);
        }
        oilRow.setConsumeList(oilConsumeList);
        oilRow.setEnergyType(energyType.getName());
        oilRow.setUnit(energyType.getMeasureUnitName());
        oilRow.setDepartmentName("基片");
        oilRow.setSumStr(BigDecimalUtil.format(sum));
        return oilRow;
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
