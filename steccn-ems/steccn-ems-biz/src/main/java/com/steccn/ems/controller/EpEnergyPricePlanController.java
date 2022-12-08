package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyPriceDTO;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyPricePlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api("能源价格方案")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/energy/price/plan")
public class EpEnergyPricePlanController {

    private final EpEnergyPricePlanService epEnergyPricePlanService;

    @PostMapping
    @ApiOperation(value = "新增或编辑能源价格方案")
    @SysLog("新增或编辑能源价格方案")
    public R<Boolean> addOrEdit(@Valid @RequestBody EpEnergyPriceDTO dto) {
        return R.success(epEnergyPricePlanService.addOrEdit(dto));
    }
}
