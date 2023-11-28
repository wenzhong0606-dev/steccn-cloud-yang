package com.steccn.wms.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryRecordService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsTransferSlipService;
import com.steccn.wms.vo.WmsDocumentQuantityVO;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

    @PostMapping("/opening/inventory")
    @ApiOperation("获取期初库存")
    public R<BigDecimal>  openingInventory(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsInventoryRecordService.openingInventory(wmsReportWarehouseDTO));
    }


    @PostMapping("/ending/inventory")
    @ApiOperation("获取期末库存")
    public R<BigDecimal>  endingInventory(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsInventoryRecordService.endingInventory(wmsReportWarehouseDTO));
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
    public R<EpChartGraphVO<List<String>, BigDecimal>> warehousingClassification(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsStorageReceiptService.warehousingClassification(wmsReportWarehouseDTO));
    }

    @PostMapping("/outbound/delivery/classification")
    @ApiOperation("出库单分类")
    public R<EpChartGraphVO<List<String>, BigDecimal>> outboundDeliveryClassification(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsDeliveryReceiptService.outboundDeliveryClassification(wmsReportWarehouseDTO));
    }


    @PostMapping("/transfer/slip/classification")
    @ApiOperation("调拨单分类分析")
    public R<EpChartGraphVO<List<String>, BigDecimal>> transferSlipClassification(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsTransferSlipService.transferSlipClassification(wmsReportWarehouseDTO));
    }

    @PostMapping("/document/quantity")
    @ApiOperation("单据数量")
    public R<WmsDocumentQuantityVO> documentQuantity(WmsReportWarehouseDTO dto) {
        WmsDocumentQuantityVO wmsDocumentQuantityVO = new WmsDocumentQuantityVO();
        long count1 = wmsStorageReceiptService.count(new LambdaQueryWrapper<WmsStorageReceipt>()
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsStorageReceipt::getCreateTime, dto.getStartTime(), dto.getEndTime())
                .in(CollUtil.isNotEmpty(dto.getWarehouseIds()), WmsStorageReceipt::getWarehouseId, dto.getWarehouseIds())
                .notIn(WmsStorageReceipt::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode()));
        wmsDocumentQuantityVO.setInboundSingular((int) count1);
        wmsDocumentQuantityVO.setOutboundSingular((int) (wmsDeliveryReceiptService.count(new LambdaQueryWrapper<WmsDeliveryReceipt>()
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsDeliveryReceipt::getCreateTime, dto.getStartTime(), dto.getEndTime())
                .in(CollUtil.isNotEmpty(dto.getWarehouseIds()), WmsDeliveryReceipt::getWarehouseId, dto.getWarehouseIds())
                .notIn(WmsDeliveryReceipt::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode()))));
        LambdaQueryWrapper<WmsTransferSlip> wrapper = new LambdaQueryWrapper<WmsTransferSlip>()
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsTransferSlip::getCreateTime, dto.getStartTime(), dto.getEndTime())
                .notIn(WmsTransferSlip::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode());
        if (CollUtil.isNotEmpty(dto.getWarehouseIds())) {
            wrapper.and(w -> {
                w.in(WmsTransferSlip::getSourceWarehouse, dto.getWarehouseIds()).or()
                        .in(WmsTransferSlip::getTargetWarehouse, dto.getWarehouseIds());
            });
        }
        wmsDocumentQuantityVO.setDialingSingular((int) wmsTransferSlipService.count(wrapper));
        return R.success(wmsDocumentQuantityVO);
    }
}

