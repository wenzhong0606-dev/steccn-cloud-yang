package com.steccn.mes.feign;

import com.steccn.common.vo.R;
import com.steccn.mes.dto.*;
import com.steccn.mes.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * erp调用
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@FeignClient(name = "remoteErpFeignApi", url = "${erp.url}", decode404 = true)

public interface RemoteErpFeignApi {

    /**
     * 获取客户信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getcust")
    @ApiOperation("获取erp客户信息")
    String getErpCustomer(@RequestBody ErpCustomerDTO dto);

    /**
     * 保存直接调拨单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savetransferdirect")
    @ApiOperation("保存直接调拨单")
    String saveTransfer(@RequestBody ErpCommonSaveDTO dto);

    /**
     * 保存简单生产入库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savespinstock")
    @ApiOperation("保存简单生产入库单")
    String saveSpinStock(@RequestBody ErpCommonSaveDTO dto);

    /**
     * 保存采购入库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/saveinstock")
    @ApiOperation("保存采购入库单")
    String saveInStock(@RequestBody ErpStockSaveDTO dto);

    /**
     * 保存销售退货单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savereturnstock")
    @ApiOperation("保存销售退货单")
    String saveReturnStock(@RequestBody ErpReturnStockSaveDTO dto);

    /**
     * 保存简单生产领料单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savesppickmtrl")
    @ApiOperation("保存简单生产领料单")
    String saveSppickmtrl(@RequestBody ErpCommonSaveDTO dto);

    /**
     * 保存其他入库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savemiscellaneous")
    @ApiOperation("保存其他入库单")
    String saveMiscellaneous(@RequestBody ErpStockSaveDTO dto);

    /**
     * 保存采购退料单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savepurmrb")
    @ApiOperation("保存采购退料单")
    String savepurmrb(@RequestBody ErpStockSaveDTO dto);

    /**
     * 保存销售出库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/saveoutstock")
    @ApiOperation("保存销售出库单")
    String saveOutStock(@RequestBody ErpReturnStockSaveDTO dto);

    /**
     * 保存其他出库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savemisdelivery")
    @ApiOperation("保存其他出库单")
    String saveMisDelivery(@RequestBody ErpReturnStockSaveDTO dto);

    /**
     * 保存生产领料单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savesppickmtrl")
    @ApiOperation("保存生产领料单")
    String saveSpPickMtrl(@RequestBody ErpReturnStockSaveDTO dto);

    /**
     * 保存其他出库单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savemisdelivery")
    @ApiOperation("保存其他出库单")
    String saveMisdelivery(@RequestBody ErpReturnStockSaveDTO dto);

    /**
     * 保存简单生产退料单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savespreturnmtrl")
    @ApiOperation("保存简单生产退料单")
    String saveSpreturnMtrl(@RequestBody ErpSppickMtrlSaveDTO dto);

    /**
     * 获取物料信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getmaterial")
    @ApiOperation("获取erp物料信息")
    R<List<ErpMaterialVO>> getErpMaterial(@RequestBody ErpCustomerDTO dto);

    /**
     * 获取物料信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getmaterial")
    @ApiOperation("获取erp物料单位")
    R<List<ErpMaterialUnitVO>> getErpMaterialUnit(@RequestBody ErpCustomerDTO dto);

    /**
     * 获取供应商信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getsupplier")
    @ApiOperation("获取供应商信息")
    R<List<ErpSupplierVO>> getErpSupplier(@RequestBody ErpCustomerDTO dto);

    /**
     * 获取erp仓库信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getstock")
    @ApiOperation("获取erp仓库信息")
    R<List<ErpStockVO>> getStock(@RequestBody ErpCustomerDTO dto);

    /**
     * 获取erp库存信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getinventory")
    @ApiOperation("获取erp库存信息")
    R<List<ErpInventoryVO>> getInventory(@RequestBody ErpInventoryDTO dto);
}
