package com.steccn.pms.controller.feign;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.WorkOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 生产订单feign
 *
 * @author Mongknife
 * @date 2022/11/3
 */

@RestController
@Api("生产订单feign")
@RequestMapping("/feign/pms/produce/order")
@RequiredArgsConstructor
@Slf4j
public class PmsProduceOrderFeignController {

    private final PmsProduceOrderService pmsProduceOrderService;

    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;

    @PostMapping("/line/ids")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    public R<Map<Integer, PmsProduceOrderDTO>> listByLineIds(@RequestBody Set<Integer> ids) {
        return R.success(pmsProduceOrderService.listByLineIds(ids));
    }

    @ApiOperation("根据产线id查询订单当前正在执行订单")
    @GetMapping("/line")
    public R<PmsProduceOrderVO> listByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId) {
        return R.success(pmsProduceOrderService.listByProduceLineId(productLineId));
    }

    @ApiOperation("根据生产订单号查询产线,车间,批次信息")
    @GetMapping("/code")
    public R<PmsProduceOrderVO> code(@ApiParam("订单号") @RequestParam("code") String code) {
        return R.success(pmsProduceOrderService.code(code));
    }

    @PostMapping("/material/ids")
    @ApiOperation("根据物料查询当前正在执行订单信息")
    public R<Map<Long, PmsProduceOrderVO>> listByMaterialIds(@RequestBody Set<Long> materialIds) {
        return R.success(pmsProduceOrderService.listByMaterialIds(materialIds));
    }

    @ApiOperation("查询当前批次信息")
    @GetMapping("/batch/id")
    @SysLog("查询当前批次信息")
    public R<PmsWorkOrderBatch> getByOrderId(@RequestParam("id") Long id) {

        return R.success(pmsWorkOrderBatchService.getByOrderId(id));
    }

    @ApiOperation("根据班次时间查询物料id")
    @GetMapping("/getMaterialIdByClassTime")
    public R<WorkOrderVO> getMaterialCodeByClassTime(@RequestParam("startTime") LocalDateTime startTime, @RequestParam("endTime") LocalDateTime endTime) {
        return R.success(pmsProduceOrderService.getMaterialCodeByClassTime(startTime, endTime));
    }
}
