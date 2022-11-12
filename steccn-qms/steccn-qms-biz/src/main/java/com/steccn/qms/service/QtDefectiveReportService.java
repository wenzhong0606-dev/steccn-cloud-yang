package com.steccn.qms.service;

import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.vo.QtChartBarVO;

/**
 * @author yequan
 * @description 缺陷项报表控制器
 * @createDate 2022-11-12 14:35:09
 */
public interface QtDefectiveReportService {


    /**
     * 深加工缺陷类别分布
     *
     * @param dto dto
     * @return
     */
    QtChartBarVO deepDefectiveCategory(QtChartParamDTO dto);
}
