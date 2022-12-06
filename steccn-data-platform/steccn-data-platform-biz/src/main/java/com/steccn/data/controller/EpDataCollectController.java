package com.steccn.data.controller;


import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.data.domain.EpEnergyPricePlan;
import com.steccn.data.service.EpEnergyPricePlanService;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yequan
 * @desc 能耗数据采集
 */
@Api("能耗数据采集")
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform/ep")
public class EpDataCollectController {

    private final EpEnergyPricePlanService epEnergyPricePlanService;

    @PostMapping
    @ApiOperation(value = "采样数据")
    public R<Boolean> add(@RequestBody EpEnergyPricePlan dto) {
        return R.success(epEnergyPricePlanService.save(dto));
    }
}
