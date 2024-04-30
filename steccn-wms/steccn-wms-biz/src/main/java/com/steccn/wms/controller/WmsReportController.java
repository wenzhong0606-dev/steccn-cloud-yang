package com.steccn.wms.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.vo.PanoramicTracingVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.dto.WmsOutInWarehouseInfoDTO;
import com.steccn.wms.dto.WmsReportStatisticalDTO;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.service.*;
import com.steccn.wms.vo.WmsDocumentQuantityVO;
import com.steccn.wms.vo.WmsReportTotalQuantityVO;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final WmsInventoryService wmsInventoryService;

    private final WmsReportService wmsReportService;

    @PostMapping("/statistical")
    @ApiOperation("仓储统计报表")
    public R<SteccnTableDataDTO> statisticalData(@RequestBody WmsReportStatisticalDTO dto){
        return R.success(wmsReportService.statisticalData(dto));
    }

    @PostMapping("out/in/warehouse/info")
    @ApiOperation("出入库明细")
    public R<SteccnTableDataDTO> outInWarehouseInfo(@RequestBody WmsOutInWarehouseInfoDTO dto){
        return R.success(wmsReportService.outInWarehouseInfo(dto));
    }


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

    @PostMapping("/inaction/stock")
    @ApiOperation("获取呆滞库存")
    public R<TupleDTO<BigDecimal,BigDecimal>>  inactionStock(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsInventoryService.inactionStock(wmsReportWarehouseDTO));
    }

    @PostMapping("/inventory")
    @ApiOperation("获取实时库存")
    public R<BigDecimal> inventory(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsInventoryService.inventory(wmsReportWarehouseDTO));
    }
    @PostMapping("/inventory/m")
    @ApiOperation("获取实时库存-平方米")
    public R<BigDecimal> inventoryM(@RequestBody WmsReportWarehouseDTO wmsReportWarehouseDTO){
        return R.success(wmsInventoryService.inventoryM(wmsReportWarehouseDTO));
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
    public R<WmsDocumentQuantityVO> documentQuantity(@RequestBody  WmsReportWarehouseDTO dto) {
        WmsDocumentQuantityVO wmsDocumentQuantityVO = new WmsDocumentQuantityVO();
        if (Objects.nonNull(dto.getDate())){
            dto.setStartTime(LocalDateUtil.getMorning(dto.getDate()));
            dto.setEndTime(LocalDateUtil.getMorning(dto.getDate()).plusDays(1));
        }
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
                .notIn(WmsTransferSlip::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode(),WmsManagerConstant.OrderStateEnum.ZERO.getCode());
        if (CollUtil.isNotEmpty(dto.getWarehouseIds())) {
            wrapper.and(w -> {
                w.in(WmsTransferSlip::getSourceWarehouse, dto.getWarehouseIds()).or()
                        .in(WmsTransferSlip::getTargetWarehouse, dto.getWarehouseIds());
            });
        }
        wmsDocumentQuantityVO.setDialingSingular((int) wmsTransferSlipService.count(wrapper));
        return R.success(wmsDocumentQuantityVO);
    }


    @PostMapping("/total/quantity")
    @ApiOperation("获取单据明细总数")
    public R<WmsReportTotalQuantityVO> totalQuantity(@RequestBody WmsReportWarehouseDTO dto) {
        WmsReportTotalQuantityVO quantityVO = new WmsReportTotalQuantityVO();
        if (Objects.nonNull(dto.getDate())) {
            dto.setStartTime(LocalDateUtil.getMorning(dto.getDate()));
            dto.setEndTime(LocalDateUtil.getMorning(dto.getDate()).plusDays(1));
        }
        //获取入库单据明细总数
        quantityVO.setWarehouseTotalQuantity(wmsStorageReceiptService.totalQuantity(dto));
        //获取出库单据明细总数
        quantityVO.setOutboundTotalQuantity(wmsDeliveryReceiptService.totalQuantity(dto));
        //获取调拨单据明细总数
        quantityVO.setTransferTotalQuantity(wmsTransferSlipService.totalQuantity(dto));
        return R.success(quantityVO);
    }

    @PostMapping("/total/quantity/m")
    @ApiOperation("获取单据明细总数")
    public R<WmsReportTotalQuantityVO> totalQuantityM(@RequestBody WmsReportWarehouseDTO dto) {
        WmsReportTotalQuantityVO quantityVO = new WmsReportTotalQuantityVO();
        if (Objects.nonNull(dto.getDate())) {
            dto.setStartTime(LocalDateUtil.getMorning(dto.getDate()));
            dto.setEndTime(LocalDateUtil.getMorning(dto.getDate()).plusDays(1));
        }
        //获取入库单据明细总数
        quantityVO.setWarehouseTotalQuantity(wmsStorageReceiptService.totalQuantityM(dto));
        //获取出库单据明细总数
        quantityVO.setOutboundTotalQuantity(wmsDeliveryReceiptService.totalQuantityM(dto));
        //获取调拨单据明细总数
        quantityVO.setTransferTotalQuantity(wmsTransferSlipService.totalQuantityM(dto));
        return R.success(quantityVO);
    }
}

