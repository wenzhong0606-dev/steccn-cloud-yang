package com.steccn.pms.service;

import com.steccn.pms.dto.SubstrateDayReportDTO;
import com.steccn.pms.vo.SubstrateDayReportVO;

/**
 * @desc 报表接口
 * @author yequan
 */
public interface ReportService {

    /**
     * 基片日报表
     * @param dto dto
     * @return SubstrateDayReportVO
     */
    SubstrateDayReportVO substrateDayReport(SubstrateDayReportDTO dto);
}
