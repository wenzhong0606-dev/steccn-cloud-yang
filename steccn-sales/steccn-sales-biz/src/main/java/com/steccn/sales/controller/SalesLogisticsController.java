package com.steccn.sales.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.dto.KsShippingReconciliationDTO;
import com.steccn.sales.service.KsContractService;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.service.KsSendNoticeService;
import com.steccn.sales.vo.KsLogisticsVO;
import com.steccn.sales.vo.KsShippingReconciliationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * 销售系统控制层
 *
 * @author AndrewLuo
 * @date 2023/11/29
 */
@Api("销售系统物流单控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/logistics")
public class SalesLogisticsController {
    private final KsLogisticsOrderService ksLogisticsOrderService;
    private final KsSendNoticeService ksSendNoticeService;
    private final KsContractService ksContractService;

    @PostMapping("/page")
    @ApiOperation("物流单")
    R<Page<KsLogisticsVO>> getLogisticsRecord(@Valid @RequestBody KsLogisticsDTO dto){
        return R.success(ksLogisticsOrderService.getLogisticsRecord(dto));
    }

    @PostMapping("/dashBoard")
    @ApiOperation("图表")
    R<QtLineChartBarDTO> logisticsRecordDashBoard(@Valid @RequestBody KsLogisticsDTO dto){
        return R.success(ksLogisticsOrderService.logisticsRecordDashBoard(dto));
    }

    @PostMapping("/contract/dashBoard")
    @ApiOperation("下单量图表")
    R<QtLineChartBarDTO> contractDashboard(@RequestBody @Valid DpPmsOperateDTO dto){
        return R.success(ksContractService.contractSum(dto));
    }

    @PostMapping("/shipping/reconciliation")
    @ApiOperation("发货对账")
    R<Page<KsShippingReconciliationVO>> shippingReconciliation(@RequestBody KsShippingReconciliationDTO dto){
        return R.success(ksLogisticsOrderService.shippingReconciliation(dto));
    }

    @GetMapping("/dashBoard/orderQuantity/customer")
    @ApiOperation("客户下单量")
    R<EpDeepConsumeContrastVO> orderQuantityByCus(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end,@RequestParam("type")Integer type){
        return R.success(ksSendNoticeService.orderQuantityByCus(start,end,type));
    }

    @GetMapping("/dashBoard/orderQuantity/spec")
    @ApiOperation("下单产品规格")
    R<EpDeepConsumeContrastVO> orderQuantityBySpe(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end,@RequestParam("type")Integer type){
        return R.success(ksSendNoticeService.orderQuantityBySpe(start,end,type));
    }

    @GetMapping("/dashBoard/orderQuantity/attr")
    @ApiOperation("下单产品属性")
    R<EpDeepConsumeContrastVO> orderQuantityByAttr(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end,@RequestParam("type")Integer type){
        return R.success(ksSendNoticeService.orderQuantityByAttr(start,end,type));
    }

    @GetMapping("/dashBoard/orderQuantity/cls")
    @ApiOperation("下单产品分类")
    R<EpDeepConsumeContrastVO> orderQuantityByCls(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end,@RequestParam("type")Integer type){
        return R.success(ksSendNoticeService.orderQuantityByCls(start,end,type));
    }

    @GetMapping("/dashBoard/orderQuantity/thick")
    @ApiOperation("下单产品厚度")
    R<EpDeepConsumeContrastVO> orderQuantityByThick(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end,@RequestParam("type")Integer type){
        return R.success(ksSendNoticeService.orderQuantityByThick(start,end,type));
    }

    @GetMapping("/dashBoard/orderQuantity/log")
    @ApiOperation("物流单")
    R<EpDeepConsumeContrastVO> logisticsNum(@RequestParam("start")LocalDate start, @RequestParam("end") LocalDate end){
        return R.success(ksLogisticsOrderService.logisticsNum(start,end));
    }
}
