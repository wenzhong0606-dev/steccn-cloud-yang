package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EqMeterReadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/11/30 15:30
 */
@Api("抄表管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/meter/read")
public class EpMeterReadController {

    private EpMeterReadService epMeterReadService;

    @GetMapping
    @ApiOperation(value = "抄表管理列表")
    public R<EqMeterReadVO> addOrEdit(@RequestParam("readMonth") String readMonth, @RequestParam("energyType") Integer energyType) {
        return R.success(epMeterReadService.list(readMonth, energyType));
    }
}
