package com.steccn.ems.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.QtChartPieVO;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.vo.CeCarbonSumReportVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 碳排放报表接口
 */
public interface CeCarbonReportService {

    /**
     * 碳排放总量报表
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    CeCarbonSumReportVO carbonSumReport(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 碳排放强度报表
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    CeCarbonSumReportVO carbonStrongReport(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 分析看板-碳排放总量，总产量，碳排放强度
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    List<BigDecimal> analysisCarbonStrongValue(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 分析看板-碳排放总量趋势
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    QtLineChartBarDTO analysisSumLine(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 分析看板-总产量趋势
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    QtLineChartBarDTO analysisPmLine(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 分析看板-碳排放强度趋势
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    QtLineChartBarDTO analysisUnitLine(LocalDate start, LocalDate end, Integer granularity);

    /**
     * 分析看板-碳排放类别分布
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    QtChartPieVO analysisCategoryPie(LocalDate start, LocalDate end, Integer granularity);
}
