package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpPmsConsumeReportService;
import com.steccn.ems.vo.DeepProcessConsumeReportVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.ems.vo.PmElectricConsumeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产能耗报表
 * @date 2023/8/17
 */

@Api("生产看板能源控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/pm")
public class EpPmsConsumeReportController {

    private final EpPmsConsumeReportService consumeReportService;

    @GetMapping("/consume/use/electric")
    @ApiOperation(value = "获取今日用电量")
    public R<EpPmsConsumeVO> consumeUseElectric(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseElectric(officeId, date, date));
    }

    @GetMapping("/consume/use/electric/date/range")
    @ApiOperation(value = "获取日期范围用电量")
    public R<EpPmsConsumeVO> consumeUseElectricDataRange(@RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.consumeUseElectric(officeId, start, end));
    }

    @GetMapping("/consume/use/water")
    @ApiOperation(value = "获取今日用水量")
    public R<EpPmsConsumeVO> consumeUseWater(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseWater(officeId, date, date));
    }

    @GetMapping("/consume/use/water/date/range")
    @ApiOperation(value = "获取日期范围用水量")
    public R<EpPmsConsumeVO> consumeUseWaterDateRange(@RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.consumeUseWater(officeId, start, end));
    }

    @GetMapping("/consume/use/gas")
    @ApiOperation(value = "获取今日天然气使用量")
    public R<EpPmsConsumeVO> consumeUseGas(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseGas(officeId, date, date));
    }

    @GetMapping("/consume/use/gas/date/range")
    @ApiOperation(value = "获取日期范围天然气使用量")
    public R<EpPmsConsumeVO> consumeUseGasDateRange(@RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.consumeUseGas(officeId, start, end));
    }

    @GetMapping("/consume/use/oil")
    @ApiOperation(value = "获取今日重油使用量")
    public R<EpPmsConsumeVO> consumeUseOil(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseOil(officeId, date, date));
    }

    @GetMapping("/consume/use/oil/date/range")
    @ApiOperation(value = "获取日期范围重油使用量")
    public R<EpPmsConsumeVO> consumeUseOilDateRange(@RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.consumeUseOil(officeId, start, end));
    }

    @GetMapping("/consume/use/unit/consume")
    @ApiOperation(value = "获取今日能源单耗")
    public R<EpPmsConsumeVO> consumeUseUnitConsume(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseUnitConsume(officeId, date, date));
    }

    @GetMapping("/consume/use/unit/consume/date/range")
    @ApiOperation(value = "获取日期范围能源单耗")
    public R<EpPmsConsumeVO> consumeUseUnitConsumeDateRange(@RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.consumeUseUnitConsume(officeId, start, end));
    }

    @GetMapping("/consume/use/deep/report")
    @ApiOperation(value = "深加工消耗统计分析")
    public R<List<DeepProcessConsumeReportVO>> deepProcessConsumeReport(@RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(consumeReportService.deepProcessConsumeReport(start,end));
    }
}
