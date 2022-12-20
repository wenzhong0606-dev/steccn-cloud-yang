package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsWarehouseMaterialAllocation;
import com.steccn.wms.dto.WmsWarehouseMaterialAllocationDTO;
import com.steccn.wms.service.WmsWarehouseMaterialAllocationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 仓储物料配置
 * </p>
 *
 * @author yss
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wms/material/configure")
public class WmsWarehouseMaterialAllocationController {

    @Autowired
    private WmsWarehouseMaterialAllocationService pmsWarehouseMaterialAllocationService;

    /**
     * @explain: 获取仓储物料配置列表
     * @Date
     **/
    @GetMapping("/getMaterialConfigureList")
    @ApiOperation(value = "获取仓储物料配置列表分页")
    public R<Page<WmsWarehouseMaterialAllocation>> getMaterialConfigureList( WmsWarehouseMaterialAllocationDTO dto) {

        return R.success(pmsWarehouseMaterialAllocationService.getMaterialConfigureList(dto));
    }

    @PostMapping("/addOrUpdateMaterialConfigure")
    @ApiOperation(value = "新增或者编辑获取仓储物料配置")
    public R<Boolean> addOrUpdateMaterialConfigure(@RequestBody WmsWarehouseMaterialAllocation dto) {
        return  pmsWarehouseMaterialAllocationService.addOrUpdateMaterialConfigure(dto);
    }
    @DeleteMapping("/delMaterialConfigure")
    @ApiOperation(value = "删除仓储物料配置")
    public R<Boolean> delMaterialConfigure( WmsWarehouseMaterialAllocation dto) {
        return R.success(pmsWarehouseMaterialAllocationService.delMaterialConfigure(dto));
    }

}
