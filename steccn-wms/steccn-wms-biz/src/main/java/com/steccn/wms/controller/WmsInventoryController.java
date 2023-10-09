package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.dto.MessMaterialConsumeDTO;
import com.steccn.pms.dto.MessMaterialStorageDTO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.pms.vo.WmsStockVO;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsInventoryService;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 库存信息
 *
 * @author Mongknife
 * @date 2022/12/9
 */

@RestController
@RequestMapping("/wms/inventory")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryController {

    private final WmsInventoryService wmsInventoryService;

    @ApiOperation("库存分页列表")
    @PostMapping("/list")
    public R<Page<WmsInventoryVO>> pageList(@RequestBody WmsInventoryPageDTO dto) {
        return R.success(wmsInventoryService.pageList(dto));
    }

    @ApiOperation("冻结")
    @PutMapping("/freeze")
    public R<Boolean> freeze(@RequestBody WmsInventoryDTO dto) {
        Assert.notNull(dto.getId(), "库存id不能为空");
        return R.success(wmsInventoryService.freeze(dto));
    }

    @ApiOperation("解冻")
    @PutMapping("/unFreeze")
    public R<Boolean> unFreeze(@RequestBody WmsInventoryDTO dto) {
        Assert.notNull(dto.getId(), "库存id不能为空");
        return R.success(wmsInventoryService.unfreeze(dto));
    }

    @ApiOperation("根据箱号获取仓库货架信息")
    @PostMapping("/stockByCaseNo")
    public R<Map<String, CaseSiteDTO>> stockByCaseNo(@RequestBody Set<String> caseNos) {
        return R.success(wmsInventoryService.stockByCaseNo(caseNos));
    }

    @ApiOperation("获取物料在库存中的库存数量")
    @PostMapping("/list/material")
    public R<Map<Long, BigDecimal>> listMaterialInventory(@RequestBody List<MaterialStorageDTO> list) {
        return R.success(wmsInventoryService.listMaterialInventory(list));
    }

    @PostMapping("/material/inventory")
    @ResponseBody
    public R<Map<Long, MaterialInventoryDTO>> materialInventory(@RequestBody Set<Long> materialIds) {
        return R.success(wmsInventoryService.materialInventory(materialIds));
    }

    @PostMapping("/material/inventory/mes")
    @ResponseBody
    public Map<Long, MaterialInventoryDTO> materialInventoryMes(@RequestBody Set<Long> materialIds) {
        return wmsInventoryService.materialInventory(materialIds);
    }

    @PostMapping("/storageList")
    @ResponseBody
    public R<List<WmsStockInventoryDTO>> materialConsume(@RequestBody List<MaterialStorageInfoDTO> list){
        return R.success(wmsInventoryService.materialConsume(list));
    }

    @ApiOperation("校验箱号是否合规")
    @PostMapping("/checkCaseNo")
    public R<Boolean> checkCaseNo(@RequestBody WmsCheckCaseNoDTO wmsCheckCaseNoDTO){
        return R.success(wmsInventoryService.checkCaseNo(wmsCheckCaseNoDTO));
    }


    @ApiOperation("根据仓库批量删除库存数据")
    @PostMapping("/delete/data")
    public R<Boolean> deleteData (){
        return R.success(wmsInventoryService.deleteData());
    }


    @ApiOperation("物料汇总列表")
    @PostMapping("/material/list")
    public R<Page<WmsInventoryVO>> materialList(@RequestBody WmsInventoryPageDTO dto) {
        return R.success(wmsInventoryService.materialList(dto));
    }
}
