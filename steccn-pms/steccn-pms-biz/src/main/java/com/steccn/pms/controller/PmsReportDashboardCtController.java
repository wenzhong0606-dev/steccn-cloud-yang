package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.service.PmsReportDashboardCtService;
import com.steccn.pms.util.PmsUnitUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 生产看板-统计看板【日期范围】
 * @date 2023/8/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/report/dashboard/ct")
@Slf4j
public class PmsReportDashboardCtController {

    private final PmsReportDashboardCtService pmsReportDashboardCtService;

    @GetMapping("/produce/class")
    @ApiOperation("累计产量总数")
    public R<QtLineChartBarDTO> produceClass(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType, @RequestParam(value = "granularity",defaultValue = "7") Integer granularity) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsReportDashboardCtService.pmDate(workShopId, start, end, granularity, unit.getField2(), unit.getField1()));
    }

    @GetMapping("/yield/rate/date")
    @ApiOperation("总成品率")
    public R<QtLineChartBarDTO> yieldRateDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.yieldRateDate(workShopId, start, end.plusDays(1)));
    }

    @GetMapping("/pm/date")
    @ApiOperation("累计产量总数")
    public R<QtLineChartBarDTO> pmDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.pmDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/in/storage/date")
    @ApiOperation("累计入库量总数")
    public R<QtLineChartBarDTO> inStorageDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.inStorageDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/pull/force/date")
    @ApiOperation("累计拉引量总数")
    public R<QtLineChartBarDTO> pullForceDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.pullForceDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/yield/rate/date/pm")
    @ApiOperation("各产线成品率-完工")
    public R<QtLineChartBarDTO> yieldRateDatePm(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.yieldRateDatePm(workShopId, start, end, granularity));
    }

    @GetMapping("/yield/rate/date/in/storage")
    @ApiOperation("各产线成品率-入库")
    public R<QtLineChartBarDTO> yieldRateDateInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.yieldRateDateInStorage(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/pm")
    @ApiOperation("各产线总产量")
    public R<QtLineChartBarDTO> lineDatePm(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDatePm(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/in/storage")
    @ApiOperation("各产线入库总数")
    public R<QtLineChartBarDTO> lineDateInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDateInStorage(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/pf")
    @ApiOperation("各产线拉引量总数")
    public R<QtLineChartBarDTO> lineDatePf(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDatePf(workShopId, start, end, granularity));
    }

}
