package com.steccn.qms.service;

import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.vo.QtDefectReportVO;

/**
 * @author yequan
 * @desc 不良报表接口
 * @date 2023/7/11
 */
public interface QtDefectReportService {


    /**
     * 缺陷列表
     * @param dto 请求
     * @return vo
     */
    QtDefectReportVO defectList(QtDefectReportDTO dto);
}
