package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysCustomerMaterialPageDTO;
import com.steccn.platform.dto.SysCustomerMaterialSaveDTO;
import com.steccn.platform.service.SysCustomerMaterialService;
import com.steccn.platform.vo.SysCustomerMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author yequan
 * @desc 客户物料控制器
 */
@Api("客户物料控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/customer/material")
public class SysCustomerMaterialController {

    private final SysCustomerMaterialService sysCustomerMaterialService;

    @PostMapping("/page")
    @ApiOperation("客户物料列表")
    public R<Page<SysCustomerMaterialVO>> page(@Valid @RequestBody SysCustomerMaterialPageDTO dto) {
        return R.success(sysCustomerMaterialService.pageList(dto));
    }

    @PostMapping
    @ApiOperation("提交客户物料列表")
    public R<Boolean> commit(@Valid @RequestBody SysCustomerMaterialSaveDTO dto) {
        return R.success(sysCustomerMaterialService.commit(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation("客户物料明细")
    public R<SysCustomerMaterialVO> info(@PathVariable("id") Long id) {
        return R.success(sysCustomerMaterialService.info(id));
    }

    @GetMapping("/main")
    @ApiOperation("客户物料")
    public R<SysCustomerMaterialVO> customerMaterial(@RequestParam("customerId") Integer customerId,
                                                     @RequestParam("materialId") Long materialId) {
        return R.success(sysCustomerMaterialService.customerMaterial(customerId, materialId));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除客户物料")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(sysCustomerMaterialService.delete(id));
    }
}
