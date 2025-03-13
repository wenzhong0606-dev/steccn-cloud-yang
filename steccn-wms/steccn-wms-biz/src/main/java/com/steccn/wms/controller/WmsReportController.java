package com.steccn.wms.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.pms.vo.PanoramicTracingVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.*;
import com.steccn.wms.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;


    @PostMapping("/day/sheet")
    @ApiOperation("仓储入库统计日报表")
    public  R<List<WmsInventoryDaySheetVO>> daySheetData(@RequestBody WmsInventoryDaySheetDTO dto){
        return R.success(wmsReportService.daySheetData(dto));
    }

    @PostMapping("/day/sheet/case/list")
    @ApiOperation("仓储入库统计日报箱列表")
    public R<List<WmsInventoryDaySheetCaseVO>> daySheetCaseList(@RequestBody WmsInventoryDaySheetDTO dto){
        return R.success(wmsReportService.daySheetCaseList(dto));
    }

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

    @GetMapping("/dataTwin/inventory/m")
    @ApiOperation("数字孪生-平方米")
    public R<TupleDTO> inventoryMDataTwin(HttpServletRequest request){
//        validRequestHeader(request);
        return R.success(wmsInventoryService.dataTwinInventoryM());
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

    @ApiOperation("物流单查询")
    @PostMapping("/logistics/bill")
    R<Page<SalesLogisticsBillVO>> logisticsBill(@RequestBody SalesLogisticsBillDTO dto){

        return R.success(wmsDeliveryReceiptService.shippingReconciliation(dto));
    }


    @GetMapping("/finished/product/total/quantity")
    @ApiOperation("库存实时总数（成品库）")
    public R<BigDecimal> finishedProductTotalQuantity(){
        return R.success(wmsReportService.finishedProductTotalQuantity());
    }


    @GetMapping("/finished/product/lock/total/quantity")
    @ApiOperation("库存实时锁定总数（成品库）")
    public R<BigDecimal> finishedProductLockTotalQuantity(){
        return R.success(wmsReportService.finishedProductLockTotalQuantity());
    }

    @GetMapping("/finished/product/on/the/way/total/quantity")
    @ApiOperation("库存实时在途总数（成品库）")
    public R<BigDecimal> finishedProductOnTheWayTotalQuantity(){
        return R.success(wmsReportService.finishedProductOnTheWayTotalQuantity());
    }

    @ApiOperation("库存准确率")
    @PostMapping("/inventory/accuracy")
    public R<String> inventoryAccuracy(){
        return R.success(wmsReportService.inventoryAccuracy());
    }


    @ApiOperation("破损率")
    @PostMapping("/breakage/rate")
    public R<String> breakageRate(){
        return R.success(wmsReportService.breakageRate());
    }

    @PostMapping("/inaction/stock/proportion")
    @ApiOperation("呆滞库存占比")
    public R<String> inactionStockProportion(){
        return R.success(wmsReportService.inactionStockProportion());
    }


    @ApiOperation("库存周转率")
    @PostMapping("/inventory/carry/rate")
    public R<String> inventoryCarryRate(){
        return R.success(wmsReportService.inventoryCarryRate());
    }


    @ApiOperation("历史库存查询")
    @PostMapping("/historical")
    public R<Page<WmsHistoricalInventoryVO>> historical(@RequestBody WmsInventoryPageDTO dto){
        return R.success(wmsReportService.historical(dto));
    }

    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }


}

