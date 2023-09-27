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
    public R<QtLineChartBarDTO> produceClass(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsReportDashboardCtService.produceClass(workShopId, start, end, unit));
    }

    @GetMapping("/yield/rate/date")
    @ApiOperation("总成品率")
    public R<QtLineChartBarDTO> yieldRateDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.yieldRateDate(workShopId, start, end));
    }

    @GetMapping("/pm/date")
    @ApiOperation("累计产量总数")
    public R<QtLineChartBarDTO> pmDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.pmDate(workShopId, start, end, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/in/storage/date")
    @ApiOperation("累计入库量总数")
    public R<QtLineChartBarDTO> inStorageDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.inStorageDate(workShopId, start, end, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/pull/force/date")
    @ApiOperation("累计拉引量总数")
    public R<QtLineChartBarDTO> pullForceDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.pullForceDate(workShopId, start, end, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/yield/rate/date/pm")
    @ApiOperation("完工成品率")
    public R<QtLineChartBarDTO> yieldRateDatePm(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("type") Integer type) {
        Integer dateType;
        if (type == 0) {
            dateType = 1;
        } else {
            dateType = 7;
        }
        return R.success(pmsReportDashboardCtService.yieldRateDatePm(workShopId, start, end, dateType));
    }

    @GetMapping("/yield/rate/date/in/storage")
    @ApiOperation("入库成品率")
    public R<QtLineChartBarDTO> yieldRateDateInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("type") Integer type) {
        return R.success(pmsReportDashboardCtService.yieldRateDate(workShopId, start, end));
    }

}
