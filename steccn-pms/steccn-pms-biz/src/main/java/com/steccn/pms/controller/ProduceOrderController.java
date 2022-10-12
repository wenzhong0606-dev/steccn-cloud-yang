package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 生产订单相关内容
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@RestController
@RequestMapping("/pms/produce/order")
@RequiredArgsConstructor
@Slf4j
public class ProduceOrderController {

    private final PmsProduceOrderService pmsProduceOrderService;

    @ApiOperation("生产订单新增")
    @PostMapping()
    public R<Boolean> add(@RequestBody PmsProduceOrderDTO dto) {
        return R.success(pmsProduceOrderService.add(dto));
    }

    @ApiOperation("生产订单分页列表")
    @PostMapping("/list")
    public R<Page<PmsProduceOrderVO>> pageList(@RequestBody PmsProduceOrderPageDTO dto) {
        return R.success(pmsProduceOrderService.pageList(dto));
    }

    @ApiOperation("生产订单编辑")
    @PutMapping
    public R<Boolean> edit(@RequestBody PmsProduceOrderDTO dto) {
        return R.success(pmsProduceOrderService.edit(dto));
    }

    @ApiOperation("获取生产订单批次分页列表")
    @PostMapping("/batch/info")
    public R<Page<PmsWorkOrderBatchVO>> batchPageList(@RequestBody PmsProduceOrderBatchDTO dto){
        return R.success(pmsProduceOrderService.batchPageList(dto));
    }

    @ApiOperation("工序报工-订单批次的信息")
    @GetMapping("/report/work/order")
    public R<ReportWorkOrderVO> reportWorkOrder(@ApiParam("产线id") Integer productLineId, @ApiParam("工位id") Integer stationId) {
        return R.success(pmsProduceOrderService.reportWorkOrder(productLineId, stationId));
    }
}
