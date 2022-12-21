package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsDwService;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.service.PmsProcessOutStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 数仓控制器
 */
@Api("数仓控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/dw")
public class PmsDwController {

    private final PmsDwService pmsDwService;

    @GetMapping("/in/count")
    @ApiOperation(value = "进站计数归档")
    public R<Boolean> inStation(@RequestParam("date") LocalDate date) {
        return R.success(pmsDwService.inStation(date));
    }

    @GetMapping("/out/count")
    @ApiOperation(value = "出站计数归档")
    public R<Boolean> outStation(@RequestParam("date") LocalDate date) {
        return R.success(pmsDwService.outStation(date));
    }

}
