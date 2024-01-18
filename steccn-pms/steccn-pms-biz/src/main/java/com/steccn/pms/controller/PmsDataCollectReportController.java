package com.steccn.pms.controller;

import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.PmsDataCollectReportService;
import com.steccn.pms.vo.SubstrateDayReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 数采报表控制器
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@RestController
@RequestMapping("/pms/dataCollect/report")
@RequiredArgsConstructor
@Slf4j
public class PmsDataCollectReportController {
    private final PmsDataCollectReportService pmsDataCollectReportService;

    @GetMapping("/turbine")
    @ApiOperation("余热汽轮机报表")
    public R<SteccnTableDataDTO> turbine(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.turbineReport(date));
    }

    @GetMapping("/boiler")
    @ApiOperation("余热锅炉报表")
    public R<SteccnTableDataDTO> boiler(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.boilerReport(date));
    }

    @GetMapping("/thirtyFiveKv")
    @ApiOperation("35kv总开关报表")
    public R<SteccnTableDataDTO> thirtyFiveKv(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.thirtyFiveKvReport(date));
    }

    @GetMapping("/des")
    @ApiOperation("脱硫脱硝报表")
    public R<SteccnTableDataDTO> des(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.desReport(date));
    }

    @GetMapping("/melting1")
    @ApiOperation("1200t/d熔化日志1")
    public R<SteccnTableDataDTO> melting1(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.melting1(date));
    }

    @GetMapping("/melting2")
    @ApiOperation("1200t/d熔化日志2")
    public R<SteccnTableDataDTO> melting2(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.melting2(date));
    }

    @GetMapping("/annealing")
    @ApiOperation("退火窑日志")
    public R<SteccnTableDataDTO> annealing(@RequestParam("date") LocalDate date,@RequestParam("type") Integer type) {
        return R.success(pmsDataCollectReportService.annealing(date,type));
    }

    @GetMapping("/molding1")
    @ApiOperation("1200t/d成型作业日志(1-4)")
    public R<SteccnTableDataDTO> molding1(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.molding1(date));
    }

    @GetMapping("/molding2")
    @ApiOperation("1200t/d成型作业日志(5-8)")
    public R<SteccnTableDataDTO> molding2(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.molding2(date));
    }

}
