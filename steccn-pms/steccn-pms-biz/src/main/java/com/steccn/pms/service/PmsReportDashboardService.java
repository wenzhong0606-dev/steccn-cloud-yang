package com.steccn.pms.service;


import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.pms.vo.JpAnalysisReportVO;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.vo.DashboardProducePageVO;
import com.steccn.pms.vo.PmsDeepProcessingNumVO;
import com.steccn.pms.vo.PmsPullForceMeltingNumVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产看板服务
 * @date 2023/8/28
 */
public interface PmsReportDashboardService {

    /**
     * 今日拉引量(单位/T)
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal pullForceToday(Integer workShopId, LocalDate date);

    /**
     * 日期范围拉引量（单位/T）
     *
     * @param partId 部门id
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    BigDecimal pullForceDateRange(Integer partId, LocalDate start, LocalDate end);


    /**
     * 熔化率
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal meltingRate(Integer workShopId, LocalDate date);

    /**
     * 今日产量（单位/T/片/㎡）
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date);

    /**
     * 日期范围产量（单位/T/片/㎡）
     *
     * @param partId 基片制造部主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    List<BigDecimal> produceNumDateRange(Integer partId, LocalDate start, LocalDate end);

    /**
     * 产量合格率
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal produceNumRate(Integer workShopId, LocalDate date);

    /**
     * 日期范围产量合格率
     *
     * @param partId 制造部主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    BigDecimal produceNumRateDateRange(Integer partId, LocalDate start, LocalDate end);

    /**
     * 今日入库数（单位/T/片/㎡）
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    List<BigDecimal> inStorageToday(Integer workShopId, LocalDate date);

    /**
     * 日期范围入库数（单位/T/片/㎡）
     * @param partId 制造部主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    List<BigDecimal> inStorageDateRange(Integer partId, LocalDate start, LocalDate end);

    /**
     * 入库合格率
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal inStorageRate(Integer workShopId, LocalDate date);

    /**
     * 日期范围入库合格率
     * @param partId 制造部主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    BigDecimal inStorageRateDateRange(Integer partId, LocalDate start, LocalDate end);


    /**
     * 产线加工信息
     * @param dto 请求参数
     * @return
     */
    DashboardProducePageVO producePage(DashboardProducePageDTO dto);

    /**
     * 产线拉引量
     * @param workShopId 车间
     * @param date 日期
     * @return 柱状图
     */
    QtLineChartBarDTO linePullForce(Integer workShopId, LocalDate date);

    /**
     * 产线完工信息
     * @param workShopId 车间
     * @param date 日期
     * @return
     */
    QtLineChartBarDTO lineComplete(Integer workShopId, LocalDate date);

    /**
     * 产线入库信息
     * @param workShopId 车间
     * @param date 日期
     * @return
     */
    QtLineChartBarDTO lineInStorage(Integer workShopId, LocalDate date);

    /**
     * 拉引量熔化率
     * @param dto
     * @return
     */
    QtLineChartBarDTO pullForceMelting(DpPmsOperateDTO dto);

    /**
     * 拉引量熔化率数值
     * @param start
     * @param end
     * @return
     */
    PmsPullForceMeltingNumVO pullForceMeltingNum(LocalDate start,LocalDate end);

    /**
     * 深加工制造部-领用总数/耗用总数/使用总数
     * @param dto
     * @return
     */
    PmsDeepProcessingNumVO deepProcessingNum(PmsOperateDriverDTO dto);


    /**
     * 深加工制造部-基片数柱状图
     * @param dto
     * @return
     */
    EpChartGraphVO<List<String>, TripleDTO<BigDecimal,BigDecimal,BigDecimal>> deepProcessingHistogram(PmsOperateDriverDTO dto);

    /**
     * 基片分析报表
     * @param workshopId 车间主键
     * @param start 区间开始日期
     * @param end 区间结束日期
     * @return
     */
    List<JpAnalysisReportVO> analysisReport(Integer workshopId, LocalDate start, LocalDate end);

    /**
     * 基片分析报表 - 下载
     * @param workshopId 车间主键
     * @param start 区间开始日期
     * @param end 区间结束日期
     * @return
     */
    void analysisReportDownload(Integer workshopId, LocalDate start, LocalDate end, HttpServletResponse response) throws IOException;

    /**
     *
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
//    TripleDTO<BigDecimal,BigDecimal,BigDecimal> meltingRate(Integer workShopId, LocalDate date);
}
