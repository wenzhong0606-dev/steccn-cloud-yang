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
import java.util.List;

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

    @GetMapping("/produce/num/today")
    @ApiOperation("今日产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.produceNumToday(workShopId, date));
    }

    @GetMapping("/produce/num/rate")
    @ApiOperation("产量合格率")
    public R<BigDecimal> produceNumRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.produceNumRate(workShopId, date));
    }

    @GetMapping("/in/storage/today")
    @ApiOperation("今日入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.inStorageToday(workShopId, date));
    }

    @GetMapping("/in/storage/rate")
    @ApiOperation("入库合格率")
    public R<BigDecimal> inStorageRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.inStorageRate(workShopId, date));
    }
}
