package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api("产品单耗标准")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/product/unit/standard")
public class EpProductUnitConsumeStandardController {

    private final EpProductUnitConsumeStandardService unitConsumeStandardService;

    @PostMapping
    @ApiOperation(value = "新增或编辑产品单耗标准")
    @SysLog("新增或编辑产品单耗标准")
    public R<Boolean> addOrEdit(@Valid @RequestBody EpProductUnitStandardDTO dto) {
        return R.success(unitConsumeStandardService.addOrEdit(dto));
    }

    @PostMapping("/select")
    @ApiOperation(value = "查询产品单耗标准分页列表")
    public R<Page<EpProductUnitConsumeStandardVO>> getEpProductUnitConsumeStandardList(@Valid @RequestBody
                                                                                       EpProductUnitStandardPageDTO dto){
        return R.success(unitConsumeStandardService.getEpProductUnitConsumeStandardList(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询产品单耗标准")
    public R<EpProductUnitConsumeStandardVO> getProductUnitStandardVO(@PathVariable("id") Integer id) {
        return R.success(unitConsumeStandardService.getProductUnitStandardVO(id));
    }
}
