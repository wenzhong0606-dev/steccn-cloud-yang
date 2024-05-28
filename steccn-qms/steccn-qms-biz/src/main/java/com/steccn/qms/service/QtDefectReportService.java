package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.qms.dto.QtFinishedRateDTO;
import com.steccn.qms.vo.DefectDayVO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author yequan
 * @desc 不良报表接口
 * @date 2023/7/11
 */
public interface QtDefectReportService {


    /**
     * 缺陷列表
     *
     * @param dto 请求
     * @return vo
     */
    QtDefectReportVO defectList(QtDefectReportDTO dto);

    /**
     * 缺陷列表
     *
     * @param dto 请求
     * @return vo
     */
    void defectListDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 落板记录列表
     *
     * @param dto - 请求
     * @return 落板记录报表
     */
    List<QtDefectFallingVO> falling(QtDefectReportDTO dto);

    /**
     * 落板记录报表下载
     * @param dto
     * @param response
     */
    void fallingDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 厚度报表
     *
     * @param dto 请求
     * @return 报表
     */
    QtCheckItemMultiAvgVO thickAvg(QtDefectReportDTO dto);

    /**
     * 厚度报表-根据物料厚度分组
     * @param dto
     * @return
     */
    QtCheckItemSingleAvgVO thickAvgMaterial(QtDefectReportDTO dto);

    /**
     * 厚度报表
     *
     * @param dto 请求
     * @return 报表
     */
    void thickAvgDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 毛边宽报表
     *
     * @param dto 请求
     * @return 报表
     */
    QtCheckItemMultiAvgVO rbwAvg(QtDefectReportDTO dto);

    /**
     * 毛边宽报表-下载
     *
     * @param dto 请求
     * @return 报表
     */
    void rbwAvgDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 粗糙度报表
     *
     * @param dto 请求参数
     * @return
     */
    QtCheckItemSingleAvgVO roughness(QtDefectReportDTO dto);

    /**
     * 粗糙度报表
     *
     * @param dto 请求参数
     * @return
     */
    void roughnessDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 透光率报表
     *
     * @param dto – 请求参数
     * @return
     */
    QtCheckItemTransmitVO transmittance(QtDefectReportDTO dto);

    /**
     * 基片二车间透光率走势图
     * @param dto
     * @return
     */
    QtLineChartBarDTO transmittanceLine(QtDefectReportDTO dto);


    /**
     * 透光率报表-下载
     *
     * @param dto – 请求参数
     * @return
     */
    void transmittanceDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 微气泡
     *
     * @param dto 请求参数
     * @return vo
     */
    QtMicroBubbleVO microBubble(QtDefectReportDTO dto);

    /**
     * 微气泡
     *
     * @param dto 请求参数
     * @return vo
     */
    void microBubbleDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 原片不良报表
     *
     * @param dto 请求参数
     * @return 原片不良报表
     */
    OriginalDefectVO originalDefect(QtDefectReportDTO dto);

    /**
     * 原片不良报表 - 下载
     *
     * @param dto 请求参数
     * @return 原片不良报表
     */
    void originalDefectDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;


    /**
     * 镀膜不良报表
     *
     * @param dto 请求参数
     * @return
     */
    CoatingFilmVO coatingFilm(QtDefectReportDTO dto);

    /**
     * 镀膜不良报表
     *
     * @param dto 请求参数
     * @return
     */
    void coatingFilmDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 不良日报表
     *
     * @param dto 请求参数
     * @return
     */
    DefectDayVO defectDay(QtDefectReportDTO dto);

    /**
     * 不良日报表
     *
     * @param dto 请求参数
     * @return
     */
    void defectDayDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 不良周报表
     *
     * @param dto 请求参数
     * @return
     */
    List<DefectCountRowVO> defectWeek(QtDefectReportDTO dto);

    /**
     * 不良周报表-下载
     *
     * @param dto 请求参数
     * @return
     */
    void defectWeekDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 不良月报表
     * @param dto – 请求参数
     * @return
     */
    List<DefectCountRowVO> defectMonth(QtDefectReportDTO dto);

    /**
     * 不良月报表 - 下载
     * @param dto – 请求参数
     * @return
     */
    void defectMonthDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 成品率报表
     * @param dto
     * @return
     */
    SteccnTableDataDTO<QtFinishedRateDTO> finishedRate(QtDefectReportDTO dto);

    /**
     * 成品率报表
     * @param dto
     * @return
     */
    void finishedRateDownload(QtDefectReportDTO dto, HttpServletResponse response) throws IOException;

}
