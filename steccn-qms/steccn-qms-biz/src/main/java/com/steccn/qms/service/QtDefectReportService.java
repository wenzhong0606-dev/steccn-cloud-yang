package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.vo.QtDefectFallingVO;
import com.steccn.qms.vo.QtDefectReportVO;
import com.steccn.qms.vo.QtThickAvgVO;

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
     * 落板记录列表
     *
     * @param dto - 请求
     * @return 落板记录报表
     */
    List<QtDefectFallingVO> falling(QtDefectReportDTO dto);

    /**
     * 厚度报表
     * @param dto 请求
     * @return 报表
     */
    QtThickAvgVO thickAvg(QtDefectReportDTO dto);
}
