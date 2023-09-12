package com.steccn.pms.controller.feign;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.dto.DwProduceOrderDTO;
import com.steccn.pms.dto.PmsDefectDayOrderBasicDTO;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.WorkOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final PmsBarcodePrintService pmsBarcodePrintService;

    @PostMapping("/report")
    @ApiOperation("查询当前班次日期的所有报工记录")
    R<List<DwCurrentReportWorkDTO>> currentDateReportWork(@RequestBody TupleDTO<LocalDate, LocalDate> tupleDTO){
        LocalDate currentDate = tupleDTO.getField1();
        if(Objects.isNull(currentDate)){
            return R.success(Lists.newArrayList());
        }
        return R.success(pmsProduceOrderService.currentDateReportWork(currentDate));
    }

    @ApiOperation("获取当前班次时间内的生产订单")
    @PostMapping("/dw/current")
    public R<List<DwProduceOrderDTO>> currentProduceOrder(@RequestBody TupleDTO<LocalDate, LocalDate> tupleDTO) {
        LocalDate currentDate = tupleDTO.getField1();
        if(Objects.isNull(currentDate)){
            return R.success(Lists.newArrayList());
        }
        return R.success(pmsProduceOrderService.currentProduceOrder(currentDate));
    }

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


    @ApiOperation("根据产线id查询订单")
    @GetMapping("/line/order")
    public R<List<PmsProduceOrderVO>> listOrderByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId) {
        return R.success(pmsProduceOrderService.listOrderByProduceLineId(productLineId));
    }

    @ApiOperation("根据生产订单号查询产线,车间,批次信息")
    @GetMapping("/code")
    public R<PmsProduceOrderVO> code(@ApiParam("订单号") @RequestParam("code") String code) {
        return R.success(pmsProduceOrderService.code(code));
    }

    @ApiOperation("根据生产订单号查询产线,车间,批次信息")
    @GetMapping("/allCode")
    public R<PmsProduceOrderVO> allCode(@ApiParam("订单号") @RequestParam("code") String code) {
        return R.success(pmsProduceOrderService.allCode(code));
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

    @ApiOperation("查询所有订单相关的批次信息")
    @GetMapping("/all/batch/id")
    @SysLog("查询当前批次信息")
    public R<List<PmsWorkOrderBatch>> getAllBatchByOrderId(@RequestParam("id") Long id) {
        return R.success(pmsWorkOrderBatchService.getAllBatchByOrderId(id));
    }

    @ApiOperation("根据班次时间查询物料id")
    @GetMapping("/getMaterialIdByClassTime")
    public R<WorkOrderVO> getMaterialCodeByClassTime(@RequestParam("startTime") LocalDateTime startTime, @RequestParam("endTime") LocalDateTime endTime) {
        return R.success(pmsProduceOrderService.getMaterialCodeByClassTime(startTime, endTime));
    }

    @ApiOperation("查询不良日报表")
    @PostMapping("/defect")
    R<List<PmsDefectDayOrderBasicDTO>> defectDay(@RequestBody TripleDTO<List<Integer>,LocalDateTime,LocalDateTime> dto){
        return R.success(pmsWorkOrderBatchService.defectDay(dto.getField1(),dto.getField2(),dto.getField3()));
    }

    @GetMapping("/get/caseNo/flag")
    @ApiOperation(("箱号是否存在"))
    R<Boolean> getCaseNo(@RequestParam("caseNo") String caseNo){
        return R.success(pmsBarcodePrintService.getCaseNoFlag(caseNo));
    }
}
