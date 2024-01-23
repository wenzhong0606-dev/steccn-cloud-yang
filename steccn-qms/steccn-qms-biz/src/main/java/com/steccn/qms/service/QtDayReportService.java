package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.qms.dto.QtLossReportDTO;
import com.steccn.qms.dto.QtReportFirstPatrolDTO;
import com.steccn.qms.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-接口
 */
public interface QtDayReportService {

    /**
     * 质量日报表-在线检
     * @param date 指定日期
     * @param productLineId 产线id
     * @return vo
     */
    QtDayReportOnlineVO online(LocalDate date, List<Integer> productLineId,String source);

    /**
     * 在线检
     * @param date 日期
     * @param lineIdList 产线
     * @param confContent 配置
     */
    void onlineDownload(LocalDate date, List<Integer> lineIdList, String confContent, HttpServletResponse response) throws IOException;


    /**
     * 质量日报表-首检2/巡检4
     * @param dto
     * @return
     */
    QtDayReportFirstVO first(QtReportFirstPatrolDTO dto);

    /**
     * 质量日报表-首检2/巡检4-下载
     * @param dto dto
     * @param response 响应
     */
    void firstDownload(QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException;


    /**
     * 质量日报表-巡检-缺陷项
     * @param dto
     * @return
     */
    QtDayPollingFirstVO pollingDefectItem(QtReportFirstPatrolDTO dto);

    /**
     * 配料抽检日报表
     * @param date 日期
     * @return 报表
     */
    QtDaySamplingVO sampling(LocalDate date);

    /**
     * 配料含水率
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return
     */
    QtLineChartBarDTO samplingWaterRate(LocalDate start, LocalDate end);

    /**
     * 配料含水率-硅砂
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return
     */
    QtLineChartBarDTO samplingWaterRateSi(LocalDate start, LocalDate end);

    /**
     * 配料含水率-配合料
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return
     */
    QtLineChartBarDTO samplingWaterRateBatch(LocalDate start, LocalDate end);

    /**
     * 配料抽检日报表-下载
     * @param date 日期
     * @return 报表
     */
    void samplingDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 基片损耗日报表
     * @param dto
     * @return
     */
    QtDayReportLossVO loss(QtLossReportDTO dto);

    /**
     * 基片损耗日报表
     * @param dto
     * @return
     */
    void lossDownload(QtLossReportDTO dto, HttpServletResponse response) throws IOException;
}
