package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpPmsConsumeReportService;
import com.steccn.ems.vo.EpPmsConsumeVO;
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
        return R.success(consumeReportService.consumeUseElectric(officeId, date));
    }

    @GetMapping("/consume/use/water")
    @ApiOperation(value = "获取今日用水量")
    public R<EpPmsConsumeVO> consumeUseWater(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseWater(officeId, date));
    }

    @GetMapping("/consume/use/gas")
    @ApiOperation(value = "获取今日天然气使用量")
    public R<EpPmsConsumeVO> consumeUseGas(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseGas(officeId, date));
    }

    @GetMapping("/consume/use/oil")
    @ApiOperation(value = "获取今日重油使用量")
    public R<EpPmsConsumeVO> consumeUseOil(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success(consumeReportService.consumeUseOil(officeId, date));
    }

    @GetMapping("/consume/electric")
    @ApiOperation(value = "能耗信息-电耗")
    public R<List<EpPmsConsumeVO>> electric(@RequestParam("officeId") Integer officeId, @RequestParam("date") LocalDate date) {
        return R.success();
    }

}
