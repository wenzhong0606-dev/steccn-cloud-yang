package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.vo.DashboardProducePageVO;
import com.steccn.pms.vo.PmsPullForceMeltingNumVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping("/pull/force/date/range")
    @ApiOperation("日期范围拉引量（单位/T）")
    public R<BigDecimal> pullForceDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.pullForceDateRange(partId, start, end));
    }

    @GetMapping("/pull/melting/today")
    @ApiOperation("熔化率（单位/T）")
    public R<BigDecimal> meltingRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.meltingRate(workShopId, date));
    }

    @GetMapping("/produce/num/today")
    @ApiOperation("今日产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumToday(workShopId, date));
    }

    @GetMapping("/produce/num/date/range")
    @ApiOperation("日期范围产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.produceNumDateRange(partId, start, end));
    }

    @GetMapping("/produce/num/rate")
    @ApiOperation("产量合格率")
    public R<BigDecimal> produceNumRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumRate(workShopId, date));
    }

    @GetMapping("/produce/num/rate/date/range")
    @ApiOperation("日期范围产量合格率")
    public R<BigDecimal> produceNumRateDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.produceNumRateDateRange(partId, start, end));
    }

    @GetMapping("/in/storage/today")
    @ApiOperation("今日入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageToday(workShopId, date));
    }

    @GetMapping("/in/storage/date/range")
    @ApiOperation("日期范围入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.inStorageDateRange(partId, start, end));
    }

    @GetMapping("/in/storage/rate")
    @ApiOperation("入库合格率")
    public R<BigDecimal> inStorageRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageRate(workShopId, date));
    }

    @GetMapping("/in/storage/rate/date/range")
    @ApiOperation("日期范围入库合格率")
    public R<BigDecimal> inStorageRateDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.inStorageRateDateRange(partId, start, end));
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

    @GetMapping("/line/in/storage")
    @ApiOperation("产线入库信息")
    public R<QtLineChartBarDTO> lineInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.lineInStorage(workShopId, date));
    }

    @PostMapping("/line/pull/force/melting")
    @ApiOperation("拉引量/熔化率")
    public R<QtLineChartBarDTO> pullForceMelting(@RequestBody @Valid DpPmsOperateDTO dto) {
        return R.success(pmsReportDashboardService.pullForceMelting(dto));
    }

    @GetMapping("/line/pull/force/melting/num")
    @ApiOperation("拉引量/熔化率/数值")
    public R<PmsPullForceMeltingNumVO> pullForceMeltingNum(@RequestParam("start") LocalDate start,@RequestParam("end")LocalDate end) {
        return R.success(pmsReportDashboardService.pullForceMeltingNum(start,end));
    }

    @GetMapping("/deep/processing/num")
    @ApiOperation("深加工制造部-领用总数/耗用总数/使用总数")
    public R<PmsPullForceMeltingNumVO> deepProcessingNum(@RequestParam("start") LocalDate start,@RequestParam("end")LocalDate end) {
        return R.success(pmsReportDashboardService.deepProcessingNum(start,end));
    }
}
