package com.steccn.wms.controller;


import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryRecordService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsTransferSlipService;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 仓库
 *
 * @author 孟乐
 * @date 2023年11月20日
 */
@RestController
@RequestMapping("/wms/report")
@RequiredArgsConstructor
@Slf4j
public class WmsReportController {

    private final WmsInventoryRecordService wmsInventoryRecordService;

    private final WmsStorageReceiptService wmsStorageReceiptService;

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;

    private final WmsTransferSlipService wmsTransferSlipService;

    @GetMapping("/opening/inventory")
    @ApiOperation("获取期初库存")
    public R<BigDecimal>  openingInventory(@RequestParam("warehouseId") Long warehouseId, @RequestParam("date")LocalDate date){
        return R.success(wmsInventoryRecordService.openingInventory(warehouseId,date));
    }


    @GetMapping("/ending/inventory")
    @ApiOperation("获取期末库存")
    public R<BigDecimal>  endingInventory(@RequestParam("warehouseId") Long warehouseId, @RequestParam("date")LocalDate date){
        return R.success(wmsInventoryRecordService.endingInventory(warehouseId,date));
    }

    @PostMapping("/warehouse/warrant")
    @ApiOperation("获取入库单")
    public R<Page<WmsReportWarehouseVO>> warehouseWarrant(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsStorageReceiptService.warehouseWarrant(wmsReportWarehouseDTO));
    }

    @PostMapping("/outbound/delivery")
    @ApiOperation("获取出库单")
    public R<Page<WmsReportWarehouseVO>> outboundDelivery(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsDeliveryReceiptService.outboundDelivery(wmsReportWarehouseDTO));
    }


    @PostMapping("/transfer/slip")
    @ApiOperation("获取调拨单")
    public R<Page<WmsReportWarehouseVO>> transferSlip(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsTransferSlipService.transferSlip(wmsReportWarehouseDTO));
    }


    @PostMapping("/warehousing/classification")
    @ApiOperation("入库单分类")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> warehousingClassification(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsStorageReceiptService.warehousingClassification(wmsReportWarehouseDTO));
    }
}
