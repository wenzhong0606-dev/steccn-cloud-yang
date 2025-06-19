package com.steccn.wms.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsTransferSlipService;
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

    private final WmsTransferSlipService wmsTransferSlipService;

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

    @ApiOperation("入库凭证新增")
    @PostMapping("/storage/add/code")
    public R<String> storageAddCode(@Validated(InsertGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.addReturnCode(dto));
    }

    @ApiOperation("出库凭证新增")
    @PostMapping("/delivery/add")
    public R<Boolean> deliveryAdd(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.generateDelivery(dto));
    }

    @ApiOperation("创建出库单")
    @PostMapping("/delivery/create")
    public R<Boolean> deliveryCreate(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.add(dto));
    }

    @ApiOperation("创建出库单(返回id)")
    @PostMapping("/delivery/create/id")
    public R<Long> deliveryCreateId(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.addId(dto));
    }

    @ApiOperation("创建调拨单")
    @PostMapping("/transfer/slip/create")
    public R<Boolean> createTransferSlip(@Validated(InsertGroup.class) @RequestBody WmsTransferSlipDTO dto) {
        return R.success(wmsTransferSlipService.add(dto));
    }

    @ApiOperation("根据货架获取物料信息")
    @PostMapping("/material/shelf")
    public R<Map<Integer,List<WmsStockInventoryDTO>>> materialByShelf(@RequestParam("set") Set<Integer> set){
        return R.success(wmsInventoryService.materialByShelf(set));
    }

    @ApiOperation("根据仓库获取物料信息")
    @PostMapping("/material/warehouse")
    public R<Map<Integer,List<WmsStockInventoryDTO>>> materialByWarehouse(@RequestParam("set") Set<Integer> set){
        return R.success(wmsInventoryService.materialByWarehouse(set));
    }

    @ApiOperation("根据关联的单据号对出库单进行批量冲销")
    @PostMapping("/charge/against/associated")
    public R<Boolean> chargeAgainstAssociated(@RequestBody List<BatchChargeAgainstDTO> dtoList){
        return R.success(wmsStorageReceiptService.chargeAgainstAssociated(dtoList));
    }

    @ApiOperation("根据关联的单据号对出库单进行批量冲销")
    @PostMapping("/charge/against/delivery")
    public R<Boolean> chargeAgainstDelivery(@RequestBody TupleDTO<Long,String> dto){
        return R.success(wmsStorageReceiptService.chargeAgainstDelivery(dto.getField1(),dto.getField2()));
    }

    @ApiOperation("连线入库生成出库单")
    @PostMapping("/connect/delivery/create")
    public R<Boolean> connectDelivery(@RequestBody WmsTransferSlipDTO dto) {
        return R.success(wmsDeliveryReceiptService.storageToReceipt(dto.getCode()));
    }
}
