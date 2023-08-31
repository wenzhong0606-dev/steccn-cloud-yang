package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.vo.DashboardProducePageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/produce/num/rate")
    @ApiOperation("产量合格率")
    public R<BigDecimal> produceNumRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumRate(workShopId, date));
    }

    @GetMapping("/in/storage/today")
    @ApiOperation("今日入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageToday(workShopId, date));
    }

    @GetMapping("/in/storage/rate")
    @ApiOperation("入库合格率")
    public R<BigDecimal> inStorageRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageRate(workShopId, date));
    }

    @PostMapping("/produce/page")
    @ApiOperation("产线加工信息")
    public R<DashboardProducePageVO> producePage(@RequestBody DashboardProducePageDTO dto) {
        return R.success(pmsReportDashboardService.producePage(dto));
    }

    @GetMapping("/line/pull/force")
    @ApiOperation("产线拉引量")
    public R<QtLineChartBarDTO> linePullForce(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.linePullForce(workShopId, date));
    }

    @GetMapping("/line/complete")
    @ApiOperation("产线完工信息")
    public R<QtLineChartBarDTO> lineComplete(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.lineComplete(workShopId, date));
    }
}
