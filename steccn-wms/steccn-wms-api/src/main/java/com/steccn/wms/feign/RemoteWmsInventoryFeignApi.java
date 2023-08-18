package com.steccn.wms.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.constant.FeignConstant;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.wms.dto.*;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
import feign.Headers;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
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
@FeignClient(contextId = "RemoteWmsInventoryFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsInventoryFeignApi {

    /**
     * 根据箱号获取仓库货架信息
     *
     * @param caseNos
     * @return
     */
    @PostMapping("/feign/wms/inventory/stockByCaseNo")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    R<Map<String, CaseSiteDTO>> stockByCaseNo(@RequestBody Set<String> caseNos);


    /**
     * 获取物料在货架中的库存数量
     *
     * @param list
     * @return
     */
    @PostMapping(value = "/feign/wms/inventory/list/material")
    @ApiOperation("获取物料在货架中的库存数量")
    R<Map<Long, BigDecimal>> listMaterialInventory(@RequestBody List<MaterialStorageDTO> list);


    /**
     * 获取物料的可用数量
     *
     * @param materialIds
     * @return
     */
    @PostMapping("/feign/wms/inventory/material/inventory")
    @ApiOperation("获取物料的库存清单")
    R<Map<Long, MaterialInventoryDTO>> materialInventory(@RequestBody Set<Long> materialIds);


    /**
     * 获取物料库存信息
     *
     * @param list 物料集合
     * @return
     */
    @PostMapping(value = "/feign/wms/inventory/storageList")
    @ApiOperation("获取物料的库存清单")
    R<List<WmsStockInventoryDTO>> materialConsume(@RequestBody List<MaterialStorageInfoDTO> list);


    /**
     * 入库凭证新增
     *
     * @param dto
     * @return wms 入库单编码
     */
    @ApiOperation("生成入库单")
    @PostMapping(value = "/feign/wms/inventory/storage/add/code")
    R<String> generateInStorageVoucher(@Validated(InsertGroup.class) @RequestBody WmsStorageReceiptDTO dto);

    /**
     * 出库凭证新增
     *
     * @param dto
     * @return
     */
    @ApiOperation("生成出库单")
    @PostMapping(value = "/feign/wms/inventory/delivery/add")
    R<Boolean> generateOutStorageVoucher(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto);

    /**
     * 创建出库单
     *
     * @param dto
     * @return
     */
    @ApiOperation("创建出库单")
    @PostMapping(value = "/feign/wms/inventory/delivery/create")
    R<Boolean> createDelivery(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto);


    /**
     * 创建调拨单
     * @param dto
     * @return
     */
    @ApiOperation("创建调拨单")
    @PostMapping(value = "/feign/wms/transfer/slip/create")
    R<Boolean> createTransferSlip(@Validated(InsertGroup.class) @RequestBody WmsTransferSlipDTO dto);


    @ApiOperation("入库凭证确认")
    @PutMapping("/wms/storage/receipt/affirm/code")
    R<Boolean> affirm(@RequestBody WmsStorageReceiptDTO dto);

    /**
     * 获取箱子位置
     *
     * @param caseNoSet 箱子位置
     * @return
     */
    @PostMapping(value = "/feign/wms/inventory/stockByCaseNo")
    @ResponseBody
    R<Map<String, CaseSiteDTO>> caseSite(@RequestBody Set<String> caseNoSet);


    @ApiOperation("根据货架获取物料信息")
    @PostMapping("/feign/wms/inventory/material/shelf")
    R<Map<Integer,List<WmsStockInventoryDTO>>> materialByShelf(@RequestParam("set") Set<Integer> set);


    @ApiOperation("根据仓库获取物料信息")
    @PostMapping("/feign/wms/inventory/material/warehouse")
    R<Map<Integer,List<WmsStockInventoryDTO>>> materialByWarehouse(@RequestParam("set") Set<Integer> set);


}
