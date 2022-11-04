package com.steccn.pms.service;

import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.SubstrateDayReportDTO;
import com.steccn.pms.vo.EnergyCountVO;
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

    /**
     * 能耗水电油气
     * @param dto dto
     * @return
     */
    EnergyCountVO consume(EnergyCountDTO dto);
}
