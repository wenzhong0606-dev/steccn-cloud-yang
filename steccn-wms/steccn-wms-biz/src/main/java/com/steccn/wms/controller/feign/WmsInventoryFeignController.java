package com.steccn.wms.controller.feign;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsStorageReceiptService;
import io.swagger.annotations.Api;
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
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@RestController
@Api("仓储库存feign")
@RequestMapping("/feign/wms/inventory")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryFeignController {

    private final WmsInventoryService wmsInventoryService;

    private final WmsStorageReceiptService wmsStorageReceiptService;

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;

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

    /**
     * 获取物料的库存清单
     */
    @PostMapping("/material/inventory")
    @ResponseBody
    public R<Map<Long, MaterialInventoryDTO>> materialInventory(@RequestBody Set<Long> materialIds) {
        return R.success(wmsInventoryService.materialInventory(materialIds));
    }

    @PostMapping("/storageList")
    @ResponseBody
    public R<List<WmsStockInventoryDTO>> materialConsume(@RequestBody List<MaterialStorageInfoDTO> list) {
        return R.success(wmsInventoryService.materialConsume(list));
    }

    @ApiOperation("入库凭证新增")
    @PostMapping("/storage/add")
    public R<Boolean> storageAdd(@Validated(InsertGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.add(dto));
    }

    @ApiOperation("出库凭证新增")
    @PostMapping("/delivery/add")
    public R<Boolean> deliveryAdd(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.generateDelivery(dto));
    }

}
