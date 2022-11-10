package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return R.success();
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
    public R<OutputCountVO> outputCount(@RequestBody EnergyCountDTO dto) {
        return R.success(reportService.outputCount(dto));
    }
}
