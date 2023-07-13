package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpDwService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 数仓控制器
 */
@Api("数仓控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/dw")
public class EpDwController {

    private final EpDwService epDwService;

    @GetMapping("/meter/read")
    @ApiOperation(value = "抄表归档-ep_meter_read_distant")
    public R<Integer> meterRead(@RequestParam("date") LocalDate date) {
        return R.success(epDwService.meterRead(date));
    }

    @PostMapping("/meter/read")
    @ApiOperation(value = "抄表归档-ep_meter_read_distant")
    public R<Integer> meterRead(@RequestBody List<LocalDate> date) {
        Integer count = 0;
        for (LocalDate ele : date) {
            Integer num = epDwService.meterRead(ele);
            count += num;
        }
        return R.success(count);
    }

    @GetMapping("/electric/money")
    @ApiOperation(value = "电费归档")
    public R<Integer> electricMoney(@RequestParam("date") LocalDate date) {
        return R.success(epDwService.electricMoney(date));
    }

    @GetMapping("/meter/chao")
    @ApiOperation(value = "抄表-ep_meter_read")
    public R<Integer> meterChao(@RequestParam("date") LocalDate date) {
        return R.success(epDwService.read(date));
    }

    @PostMapping("/meter/chao")
    @ApiOperation(value = "抄表-ep_meter_read")
    public R<Boolean> meterChao(@RequestBody List<LocalDate> date) {
        for (LocalDate ele : date) {
            epDwService.read(ele);
        }
        return R.success(Boolean.TRUE);
    }

    @GetMapping("/heat/power")
    @ApiOperation(value = "余热发电")
    public R<Boolean> heatPower(@RequestParam("date") LocalDate date) {
        return R.success(epDwService.heatPower(date));
    }
}
