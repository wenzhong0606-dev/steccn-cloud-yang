package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.*;
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

    @ApiOperation("获取订单完工入库信息")
    @PostMapping("/complete/storage")
    public R<Page<PmsCompleteStorageVO>> getCompleteStorageInfo(@RequestBody PmsCompleteStorageDTO dto) {
        return R.success(pmsProduceOrderService.getCompleteStorageInfo(dto));
    }

    @ApiOperation("获取订单投料信息")
    @PostMapping("/complete/delivery")
    public R<Page<PmsCompleteDeliveryVO>> getCompleteDeliveryInfo(@RequestBody PmsCompleteDeliveryDTO dto) {
        return R.success(pmsProduceOrderService.getCompleteDeliveryInfo(dto));
    }


    @ApiOperation("获取生产订单批次信息")
    @PostMapping("/batch/info")
    public R<Page<PmsWorkOrderBatchVO>> getBatchInfo(@RequestBody PmsProduceOrderBatchDTO dto){
        return R.success(pmsProduceOrderService.getBatchInfo(dto));
    }


    @ApiOperation("获取工艺信息-进站信息")
    @PostMapping("/in/station")
    public R<Page<PmsProcessInStationVO>> getInStation(@RequestBody PmsProcessInStationDTO dto) {
        return R.success(pmsProduceOrderService.getInStation(dto));
    }



}
