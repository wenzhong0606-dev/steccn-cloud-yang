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
}
