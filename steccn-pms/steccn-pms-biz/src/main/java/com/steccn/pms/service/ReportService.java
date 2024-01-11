package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.OutputQualityDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.SubstrateDataDTO;
import com.steccn.pms.vo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * 基片日报表-原料耗料
     * @param dto dto
     * @return list
     */
    List<SubstrateIngredientVO> subIngredientReport(PmsDayReportDTO dto);

    /**
     * 订单进度跟踪图表
     * @param workshopId
     * @return
     */
    QtLineChartBarDTO orderProgress(Integer workshopId);


    /**
     * 产量质量报表-基片
     * @param outputQualityDTO
     * @return
     */
    List<OutputQualityVO> outputQualityJp(OutputQualityDTO outputQualityDTO);


    /**
     * 擦汗了质量报表-深加工
     * @param outputQualityDTO
     * @return
     */
    List<OutputQualitySjgVO> outputQualitySjg(OutputQualityDTO outputQualityDTO);


    /**
     * 基片日报表行数据，用于成品率报表
     * @param dto
     * @return
     */
    List<SubstrateDataListVO> substrateDataList(SubstrateDataDTO dto);

    EpChartGraphVO<List<String>, BigDecimal> rawMaterialWorkshopHistogram(LocalDate startDate, LocalDate endDate);
}
