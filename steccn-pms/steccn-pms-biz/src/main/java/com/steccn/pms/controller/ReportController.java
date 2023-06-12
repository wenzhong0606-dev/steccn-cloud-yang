package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.OutputQualityDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 报表控制器
 */
@RestController
@RequestMapping("/pms/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/sub/produce")
    @ApiOperation("基片日报表-产量")
    public R<SubstrateDayReportVO> substrateDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.substrateDayReport(dto));
    }

    @PostMapping("/sub/ingredient")
    @ApiOperation("基片日报表-原料耗料")
    public R<List<SubstrateIngredientVO>> subIngredientReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.subIngredientReport(dto));
    }

    @PostMapping("/fur/produce")
    @ApiOperation("深加工日报表-产量")
    public R<FurtherProduceReportVO> furtherProduceDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.furtherProduceDayReport(dto));
    }

    @PostMapping("/fur/ingredient")
    @ApiOperation("深加工日报表-原料耗料")
    public R<FurtherIngredientReportVO> furtherIngredientDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.furtherIngredientDayReport(dto));
    }

    @PostMapping("/consume")
    @ApiOperation("能耗水电油气")
    public R<EnergyCountVO> consume(@RequestBody EnergyCountDTO dto) {
        return R.success(reportService.consume(dto));
    }

    @PostMapping("/outputCount")
    @ApiOperation("产量统计")
    public R<OutputCountVO> outputCount(@Valid @RequestBody EnergyCountDTO dto) {
        return R.success(reportService.outputCount(dto));
    }

    @GetMapping("/orderProgress/chart/{workshopId}")
    @ApiOperation("订单进度跟踪图表")
    public R<QtLineChartBarDTO> orderProgress(@PathVariable("workshopId")Integer workshopId){
        return R.success(reportService.orderProgress(workshopId));
    }

    @PostMapping("/output/quality/jp")
    @ApiOperation("产量质量报表-基片")
    public R<List<OutputQualityVO>> outputQualityJp(@RequestBody OutputQualityDTO outputQualityDTO){
        return R.success(reportService.outputQualityJp(outputQualityDTO));
    }

    @PostMapping("/output/quality/sjg")
    @ApiOperation("产量质量报表-深加工")
    public R<List<OutputQualitySjgVO>> outputQualitySjg(@RequestBody OutputQualityDTO outputQualityDTO){
        return R.success(reportService.outputQualitySjg(outputQualityDTO));
    }
}
