package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @SysLog("生产订单新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody PmsProduceOrderDTO dto) {
        return R.success(pmsProduceOrderService.add(dto));
    }

    @ApiOperation("获取订单相关客户")
    @PostMapping("/customer")
    @SysLog("获取订单相关客户")
    public R<CustomerIdAndNameVO> getCustomer(@Validated @RequestBody QmsCustomerDTO dto) {
        return R.success(pmsProduceOrderService.getCustomer(dto));
    }

    @ApiOperation("生产订单分页列表")
    @PostMapping("/list")
    public R<Page<PmsProduceOrderVO>> pageList(@RequestBody PmsProduceOrderPageDTO dto) {
        return R.success(pmsProduceOrderService.pageList(dto));
    }

    @ApiOperation("生产订单甘特图")
    @PostMapping("/ganttChart")
    public R<PmsProduceOrderGanttChartVO> ganttChart(@Valid @RequestBody PmsProduceOrderGanttChartDTO dto) {
        return R.success(pmsProduceOrderService.produceOrderGanttChart(dto));
    }


    @ApiOperation("生产订单详情")
    @GetMapping("/info/{id}")
    public R<PmsProduceOrderVO> pageList(@PathVariable("id") Long id) {
        return R.success(pmsProduceOrderService.info(id));
    }

    @ApiOperation("生产订单编辑")
    @PutMapping
    @SysLog("生产订单编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody PmsProduceOrderDTO dto) {
        return R.success(pmsProduceOrderService.edit(dto));
    }

    @ApiOperation("获取生产订单批次分页列表")
    @PostMapping("/batch/info")
    public R<Page<PmsWorkOrderBatchVO>> batchPageList(@RequestBody PmsProduceOrderBatchDTO dto) {
        Assert.notNull(dto.getProduceOrderId(), "生产订单不能为空");
        return R.success(pmsProduceOrderService.batchPageList(dto));
    }

    @ApiOperation("工序报工-订单批次的信息")
    @GetMapping("/report/work/order")
    public R<ReportWorkOrderVO> reportWorkOrder(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId,
                                                @ApiParam("工位id") @RequestParam("stationId") Integer stationId) {
        return R.success(pmsProduceOrderService.reportWorkOrder(productLineId, stationId));
    }

    @ApiOperation("工序报工-订单批次的信息")
    @GetMapping("/report/work/order/id")
    public R<ReportWorkOrderVO> reportWorkOrderId(@ApiParam("订单id") @RequestParam("orderId") Long orderId,
                                                  @ApiParam("工位id") @RequestParam("stationId") Integer stationId) {
        return R.success(pmsProduceOrderService.reportWorkOrderId(orderId, stationId));
    }

    @ApiOperation("订单接收")
    @PostMapping("/accept/page")
    public R<Page<PmsProduceOrderVO>> acceptPage(@RequestBody ProduceOrderAcceptPageDTO dto) {
        return R.success(pmsProduceOrderService.acceptPage(dto));
    }

    @ApiOperation("订单下达")
    @PutMapping("place/an/order")
    @SysLog("订单下达")
    public R<Boolean> placeAnOrder(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.placeAnOrder(dto.getId()));
    }

    @ApiOperation("取消订单下达")
    @PutMapping("cancel/the/order")
    @SysLog("取消下达")
    public R<Boolean> cancelTheOrder(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.cancelTheOrder(dto.getId()));
    }

    @ApiOperation("工序报工-订单列表")
    @PostMapping("/report/work/order/page")
    public R<Page<PmsProduceOrderVO>> reportWorkOrderPage(@RequestBody ReportWorkOrderPageDTO dto) {
        return R.success(pmsProduceOrderService.reportWorkOrderPage(dto));
    }

    @ApiOperation("订单关闭")
    @PutMapping("/close")
    @SysLog("订单关闭")
    public R<Boolean> close(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.close(dto.getId()));
    }

    @ApiOperation("订单确认")
    @PutMapping("/affirm")
    @SysLog("订单确认")
    public R<Boolean> affirm(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.affirm(dto.getId()));
    }

    @ApiOperation("订单执行")
    @PutMapping("/execute")
    @SysLog("订单执行")
    public R<Boolean> execute(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.execute(dto.getId()));
    }

    @ApiOperation("订单暂停")
    @PutMapping("/stop")
    @SysLog("订单暂停")
    public R<Boolean> stop(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.stop(dto.getId()));
    }

    @ApiOperation("根据产线id查询订单当前正在执行订单")
    @GetMapping("/line")
    public R<PmsProduceOrderVO> listByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId) {
        return R.success(pmsProduceOrderService.listByProduceLineId(productLineId));
    }

    @ApiOperation("订单完成")
    @PutMapping("/complete")
    @SysLog("订单完成")
    public R<Boolean> complete(@RequestBody PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(), "订单id不能为空");
        return R.success(pmsProduceOrderService.complete(dto.getId()));
    }

    @PostMapping("/line/ids")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    public R<Map<Integer, PmsProduceOrderDTO>> listByLineIds(@RequestBody Set<Integer> ids) {
        return R.success(pmsProduceOrderService.listByLineIds(ids));
    }

    @PostMapping("/track/report/list")
    @ApiOperation("订单跟踪计划列表")
    public R<Page<ProduceOrderTrackReportVO>> trackReportList(@RequestBody ProduceOrderTrackReportDTO dto) {
        return R.success(pmsProduceOrderService.trackReportList(dto));
    }


    @PostMapping("/material/ids")
    @ApiOperation("根据物料查询订单信息")
    public R<Map<Long, PmsProduceOrderVO>> listByMaterialIds(@RequestBody Set<Long> materialIds) {
        return R.success(pmsProduceOrderService.listByMaterialIds(materialIds));
    }


    @ApiOperation("获取正在执行中的生产订单信息")
    @GetMapping("/execute/info")
    public R<List<ProduceOrderDataInfoVO>> getExecuteOrderInfo() {
        return R.success(pmsProduceOrderService.getExecuteOrderInfo());
    }

    @ApiOperation("获取当前班次时间内的生产订单")
    @GetMapping("/dw/current")
    public R<List<DwProduceOrderDTO>> currentProduceOrder(@RequestParam("date") LocalDate currentDate) {
        return R.success(pmsProduceOrderService.currentProduceOrder(currentDate));
    }

    @ApiOperation("全景追溯")
    @PostMapping("/panoramic/tracing")
    public R<Page<PanoramicTracingVO>> tracing(@RequestBody PanoramicTracingDTO panoramicTracingDTO) {
        return R.success(pmsProduceOrderService.tracing(panoramicTracingDTO));
    }

    @ApiOperation("返洗批次")
    @PostMapping("/repaire/batch")
    public R<RepaireBatchVO> repaireBatchList(@RequestBody RepaireBatchListDTO dto) {
        return R.success(pmsProduceOrderService.repaireBatchList(dto));
    }

    @ApiOperation("外购原片不良日报表")
    @PostMapping("/outsourcing")
    public R<OutsourcingDayReportVO> outsourcing(@Valid @RequestBody DayReportCommonDTO dto) {
        return R.success(pmsProduceOrderService.outsourcing(dto));
    }

}
