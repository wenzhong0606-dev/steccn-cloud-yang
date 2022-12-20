package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpDwService;
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
@RequestMapping("/ems/dw")
public class EpDwController {

    private final EpDwService epDwService;

    @GetMapping("/meter/read")
    @ApiOperation(value = "抄表归档")
    public R<Boolean> meterRead(@RequestParam("date") LocalDate date) {
        return R.success(epDwService.meterRead(date));
    }

}
