package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 深加工当日看板
 * @date 2023/9/12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/sjg/report/dashboard")
@Slf4j
public class PmsSjgReportDashboardController {

    private final PmsSjgReportDashboardService pmsSjgReportDashboardService;

    @GetMapping("/slip")
    @ApiOperation("领用数")
    public R<BigDecimal> slip(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.slip(workShopId, date));
    }

    @GetMapping("/consume")
    @ApiOperation("耗用数")
    public R<BigDecimal> consume(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.consume(workShopId, date));
    }
}
