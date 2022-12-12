package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyPriceDTO;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyPricePlanService;
import com.steccn.ems.vo.EpEnergyPriceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @GetMapping("/selectAll")
    @ApiOperation(value = "查询所有能源价格方案")
    public R<List<EpEnergyPriceVO>> getEpEnergyPriceVOList (){
        return R.success(epEnergyPricePlanService.getEpEnergyPriceVOList());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询能源价格方案")
    public R<EpEnergyPriceVO> getEpEnergyPriceVO (@PathVariable("id") Long id){
        return R.success(epEnergyPricePlanService.getEpEnergyPriceVO(id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除能源价格方案")
    public R<Boolean> delete(@PathVariable("id") Long id){
        return R.success(epEnergyPricePlanService.delete(id));
    }
}
