package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import com.steccn.pms.vo.WorkOrderVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 生产管理feign
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@FeignClient(contextId = "RemotePmsFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemotePmsFeignApi {


    /**
     * 根据班次日期 查询当前班次日期的所有报工记录
     *
     * @param currentDate 当前日期
     * @return
     */
    @PostMapping("/feign/pms/produce/order/report")
    @ApiOperation("根据班次日期 查询当前班次日期的所有报工记录")
    R<List<DwCurrentReportWorkDTO>> currentReportWork(@RequestBody TupleDTO<LocalDate, LocalDate> currentDate);


    /**
     * 获取当前班次时间内的生产订单
     * @param tupleDTO
     * @return
     */
    @PostMapping("/feign/pms/produce/order/dw/current")
    @ApiOperation("获取当前班次时间内的生产订单")
    R<List<DwProduceOrderDTO>> currentProduceOrder(@RequestBody TupleDTO<LocalDate, LocalDate> tupleDTO);

    /**
     * 根据产线id批量获取当前产线下正在执行中的订单信息
     *
     * @param ids
     * @return
     */
    @PostMapping("/feign/pms/produce/order/line/ids")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    R<Map<Integer, PmsProduceOrderDTO>> listByLineIds(@RequestBody Set<Integer> ids);


    /**
     * 根据产线id查询订单当前正在执行订单
     *
     * @param productLineId
     * @return
     */
    @ApiOperation("根据产线id查询订单当前正在执行订单")
    @GetMapping("/feign/pms/produce/order/line")
    R<PmsProduceOrderVO> listByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId);

    /**
     * 根据生产订单号查询正在执行中任务的产线,批次,车间信息
     *
     * @param code
     * @return
     */
    @ApiOperation("根据生产订单号查询正在执行中任务的产线,批次,车间信息")
    @GetMapping("/feign/pms/produce/order/code")
    R<PmsProduceOrderVO> code(@ApiParam("生产订单号") @RequestParam("code") String code);

    /**
     * 根据物料查询当前正在执行订单信息
     *
     * @param materialIds
     * @return
     */
    @PostMapping("/feign/pms/produce/order/material/ids")
    @ApiOperation("根据物料查询当前正在执行订单信息")
    R<Map<Long, PmsProduceOrderVO>> listByMaterialIds(@RequestBody Set<Long> materialIds);

    /**
     * 根据订单表中的订单id查询批次表中当前批次的批次信息
     *
     * @param
     * @return
     */
    @GetMapping("/feign/pms/produce/order/batch/id")
    @ApiOperation("根据物料查询当前正在执行订单信息")
    R<PmsWorkOrderBatchVO> getByOrderId(@RequestParam("id") Long id);

    @ApiOperation("根据班次时间查询物料id")
    @GetMapping("/feign/pms/produce/order/getThickByClassTime")
    R<WorkOrderVO> getMaterialCodeByClassTime(@RequestParam("startTime") LocalDateTime startTime, @RequestParam("endTime")LocalDateTime endTime);

    /**
     * 领用单状态更改
     * @param dto
     * @return
     */
    @ApiOperation("领用单状态更改")
    @PutMapping("/feign/pms/material/requisition/approval")
    R<Boolean> approval(@RequestBody MaterialRequisitionApprovalDTO dto);

    /**
     * 根据requestId查询业务主键
     * @param requestId
     * @return
     */
    @ApiOperation("根据流程id查询业务id")
    @GetMapping("/feign/pms/material/requisition/requestId")
    R<Long> getIdByRequestId(@RequestParam("requestId") Long requestId);

    /**
     * 根据业务id查询状态
     * @param id
     * @return
     */
    @ApiOperation("根据业务id查询状态")
    @GetMapping("/feign/pms/material/requisition/type")
    R<Long> getStatusById(@RequestParam("id") Long id);

    @ApiOperation("根据业务id查询状态")
    @PostMapping("/feign/pms/produce/order/defect")
    R<List<PmsDefectDayOrderBasicDTO>> defectDay(@RequestBody TripleDTO<List<Integer>,LocalDateTime,LocalDateTime> dto);
}
