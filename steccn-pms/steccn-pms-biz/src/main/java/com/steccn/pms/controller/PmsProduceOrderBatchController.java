package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.WorkOrderMaterialVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生产订单号表的id查询批次表中当前批次信息
 */

@RestController
@RequestMapping("/pms/produce/order/batch")
@RequiredArgsConstructor
@Slf4j
public class PmsProduceOrderBatchController {

    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;

    private final PmsBarcodePrintService pmsBarcodePrintService;

    @ApiOperation("查询当前批次信息")
    @GetMapping("/id")
    @SysLog("查询当前批次信息")
    public R<PmsWorkOrderBatch> getByOrderId(@RequestParam("id") Long id){

        return R.success(pmsWorkOrderBatchService.getByOrderId(id));
    }


    @ApiOperation("根据箱号获取生产批次号以及物料信息")
    @GetMapping("/caseNo")
    public R<List<WorkOrderMaterialVO>> getMaterialByCaseNo(@RequestParam("code") String code){
        return R.success(pmsBarcodePrintService.getMaterialByCaseNo(code));
    }

    @ApiOperation("根据批次号获取物料信息")
    @GetMapping("/batchNo")
    public R<List<WorkOrderMaterialVO>> getMaterialByBatchNo(@RequestParam("batchNo")String batchNo){
        return R.success(pmsBarcodePrintService.getMaterialByBatchNo(batchNo));
    }

}
