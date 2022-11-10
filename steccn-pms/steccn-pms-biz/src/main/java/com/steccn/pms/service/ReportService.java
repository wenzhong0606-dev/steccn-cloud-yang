package com.steccn.pms.service;

import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.vo.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 报表接口
 */
public interface ReportService {

    /**
     * 基片日报表
     *
     * @param dto dto
     * @return SubstrateDayReportVO
     */
    SubstrateDayReportVO substrateDayReport(PmsDayReportDTO dto);

    /**
     * 能耗水电油气
     *
     * @param dto dto
     * @return
     */
    EnergyCountVO consume(EnergyCountDTO dto);

    /**
     * 产量统计
     *
     * @param dto
     * @return
     */
    OutputCountVO outputCount(EnergyCountDTO dto);

    /**
     * 构建行数据
     *
     * @param productLine
     * @param dataList
     * @param specStr
     * @return
     */
    SubstrateDayReportListVO rowData(SysOfficeVO productLine, List<BigDecimal> dataList, String specStr);

    /**
     * 深加工日报表-产量信息
     *
     * @param dto dto
     * @return
     */
    FurtherProduceReportVO furtherProduceDayReport(PmsDayReportDTO dto);

    /**
     * 深加工日报表-原料耗料
     *
     * @param dto
     * @return
     */
    FurtherIngredientReportVO furtherIngredientDayReport(PmsDayReportDTO dto);
}
