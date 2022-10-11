package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation("获取订单完工入库分页列表")
    @PostMapping("/complete/storage")
    public R<Page<PmsCompleteStorageVO>> completeStoragePageList(@RequestBody PmsCompleteStorageDTO dto) {
        return R.success(pmsProduceOrderService.completeStoragePageList(dto));
    }

    @ApiOperation("获取订单投料分页列表")
    @PostMapping("/complete/delivery")
    public R<Page<PmsCompleteDeliveryVO>> completeDeliveryPageList(@RequestBody PmsCompleteDeliveryDTO dto) {
        return R.success(pmsProduceOrderService.completeDeliveryPageList(dto));
    }


    @ApiOperation("获取生产订单批次分页列表")
    @PostMapping("/batch/info")
    public R<Page<PmsWorkOrderBatchVO>> batchPageList(@RequestBody PmsProduceOrderBatchDTO dto){
        return R.success(pmsProduceOrderService.batchPageList(dto));
    }


    @ApiOperation("工艺信息-进站分页列表")
    @PostMapping("/in/station")
    public R<Page<PmsProcessInStationVO>> inStationPageList(@RequestBody PmsProcessInStationDTO dto) {
        return R.success(pmsProduceOrderService.inStationPageList(dto));
    }

    @ApiOperation("工艺信息-出站分页列表")
    @PostMapping("/out/station")
    public R<Page<PmsProcessOutStationVO>> outStationPageList(@RequestBody PmsProcessOutStationPageDTO dto){
        return R.success(pmsProduceOrderService.outStationPageList(dto));
    }

    @ApiOperation("工艺信息-报工分页列表")
    @PostMapping("/report/work")
    public R<Page<PmsReportWorkVO>> reportWorkPageList(@RequestBody PmsReportWorkPageDTO dto){
        return R.success(pmsProduceOrderService.reportWorkPageList(dto));
    }

    @ApiOperation("工艺信息-投料信息")
    @PostMapping("/feed")
    public R<Page<PmsCompleteDeliveryVO>> feedPageList(@RequestBody PmsCompleteDeliveryDTO dto){
        return R.success(pmsProduceOrderService.feedPageList(dto));
    }


}
