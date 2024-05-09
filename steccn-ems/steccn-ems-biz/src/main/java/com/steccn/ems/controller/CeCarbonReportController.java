package com.steccn.ems.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.SteccnRow;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.vo.CeCarbonSumReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 碳排放报表控制器
 */
@Api("碳排放报表控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/ce/carbon")
public class CeCarbonReportController {

    private final CeCarbonReportService ceCarbonReportService;

    @PostMapping("/sum/report")
    @ApiOperation(value = "碳排放总量报表")
    public R<CeCarbonSumReportVO> carbonSumReport(@RequestBody CeCarbonReportDTO dto) {
        return R.success(ceCarbonReportService.carbonSumReport(dto.getStart(), dto.getEnd(), dto.getGranularity()));
    }

    @PostMapping("/strong/report")
    @ApiOperation(value = "碳排放强度报表")
    public R<CeCarbonSumReportVO> carbonStrongReport(@RequestBody CeCarbonReportDTO dto) {
        return R.success(ceCarbonReportService.carbonStrongReport(dto.getStart(), dto.getEnd(), dto.getGranularity()));
    }

    @PostMapping("/analysis/strong/value")
    @ApiOperation(value = "分析看板-碳排放总量，总产量，碳排放强度")
    public R<List<BigDecimal>> analysisCarbonStrongValue(@RequestBody CeCarbonReportDTO dto) {
        return R.success(ceCarbonReportService.analysisCarbonStrongValue(dto.getStart(), dto.getEnd(), dto.getGranularity()));
    }

    @PostMapping("/analysis/sum/line")
    @ApiOperation(value = "分析看板-碳排放总量趋势")
    public R<QtLineChartBarDTO> analysisSumLine(@RequestBody CeCarbonReportDTO dto) {
        return R.success(ceCarbonReportService.analysisSumLine(dto.getStart(), dto.getEnd(), dto.getGranularity()));
    }

}
