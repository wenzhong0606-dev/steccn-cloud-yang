package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("产品单耗标准")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/product/unit/standard")
public class EpProductUnitConsumeStandardController {

    private final EpProductUnitConsumeStandardService unitConsumeStandardService;


    @PostMapping("/select")
    @ApiOperation(value = "查询能源列表")
    public R<List<EpProductUnitConsumeStandardVO>> getEpProductUnitConsumeStandardList(){
        return R.success(unitConsumeStandardService.getEpProductUnitConsumeStandardList());
    }
}
