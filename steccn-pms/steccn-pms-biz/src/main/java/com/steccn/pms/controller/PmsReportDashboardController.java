package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsReportDashboardService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产看板
 * @date 2023/8/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/report/dashboard")
@Slf4j
public class PmsReportDashboardController {

    private final PmsReportDashboardService pmsReportDashboardService;

    @GetMapping("/pull/force/today")
    @ApiOperation("今日拉引量（单位/T）")
    public R<BigDecimal> pullForceToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.pullForceToday(workShopId, date));
    }

    @GetMapping("/produce/num/today")
    @ApiOperation("今日产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumToday(workShopId, date));
    }

}
