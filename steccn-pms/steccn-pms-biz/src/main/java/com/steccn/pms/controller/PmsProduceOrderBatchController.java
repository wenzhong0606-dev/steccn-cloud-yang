package com.steccn.pms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 生产订单号表的id查询批次表中当前批次信息
 */

@RestController
@RequestMapping("/pms/produce/order/batch")
@RequiredArgsConstructor
@Slf4j
public class PmsProduceOrderBatchController {

    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;

    @ApiOperation("查询当前批次信息")
    @GetMapping("/{id}")
    @SysLog("查询当前批次信息")
    public R<PmsWorkOrderBatch> getByOrderId(@PathVariable("id") Long id){

        return R.success(pmsWorkOrderBatchService.getByOrderId(id));
    }

}
