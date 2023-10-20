package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.service.PmsSjgReportDashboardService;
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
        return R.success(pmsSjgReportDashboardService.consume(workShopId, date, PmManagerConstant.MEASURE_UNIT_PIECE, UnitLengthUtil.UNIT_PIECE_LENGTH));
    }

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

    @GetMapping("/line/pull/force")
    @ApiOperation("产线耗用数")
    public R<QtLineChartBarDTO> linePullForce(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.lineUseCount(workShopId, date));
    }

    @GetMapping("/line/complete")
    @ApiOperation("产线完工信息")
    public R<QtLineChartBarDTO> lineComplete(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.lineComplete(workShopId, date));
    }

    @GetMapping("/line/in/storage")
    @ApiOperation("产线入库信息")
    public R<QtLineChartBarDTO> lineInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsSjgReportDashboardService.lineInStorage(workShopId, date));
    }


}
