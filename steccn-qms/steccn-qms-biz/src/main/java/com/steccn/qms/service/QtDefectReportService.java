package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
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
     * 厚度报表
     *
     * @param dto 请求
     * @return 报表
     */
    QtCheckItemMultiAvgVO thickAvg(QtDefectReportDTO dto);

    /**
     * 毛边宽报表
     *
     * @param dto 请求
     * @return 报表
     */
    QtCheckItemMultiAvgVO rbwAvg(QtDefectReportDTO dto);

    /**
     * 粗糙度报表
     *
     * @param dto 请求参数
     * @return
     */
    QtCheckItemSingleAvgVO roughness(QtDefectReportDTO dto);

    /**
     * 透光率报表
     *
     * @param dto – 请求参数
     * @return
     */
    QtCheckItemTransmitVO transmittance(QtDefectReportDTO dto);

    /**
     * 微气泡
     *
     * @param dto 请求参数
     * @return vo
     */
    QtMicroBubbleVO microBubble(QtDefectReportDTO dto);

    /**
     * 原片不良报表
     *
     * @param dto 请求参数
     * @return 原片不良报表
     */
    OriginalDefectVO originalDefect(QtDefectReportDTO dto);

    /**
     * 镀膜不良报表
     *
     * @param dto 请求参数
     * @return
     */
    CoatingFilmVO coatingFilm(QtDefectReportDTO dto);

    /**
     * 不良日报表
     *
     * @param dto 请求参数
     * @return
     */
    DefectDayVO defectDay(QtDefectReportDTO dto);

    /**
     * 不良周报表
     *
     * @param dto 请求参数
     * @return
     */
    List<DefectCountRowVO> defectWeek(QtDefectReportDTO dto);

    /**
     * 不良月报表
     * @param dto – 请求参数
     * @return
     */
    List<DefectCountRowVO> defectMonth(QtDefectReportDTO dto);

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
