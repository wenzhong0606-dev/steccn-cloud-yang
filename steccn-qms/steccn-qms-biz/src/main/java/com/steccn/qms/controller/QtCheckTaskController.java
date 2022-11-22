package com.steccn.qms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 巡检任务controller
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/task")
@Slf4j
public class QtCheckTaskController {
    private final QtCheckTaskService qtCheckTaskService;

    private final QtCheckPlanGroupItemService qtCheckPlanGroupItemService;

    @PostMapping
    @ApiOperation(value = "新建巡检任务")
    @SysLog("新建巡检任务")
    public R<Long> add(@Validated(InsertGroup.class) @RequestBody QtAddCheckTaskDTO dto) {
        return R.success(qtCheckTaskService.add(dto));
    }

    @PostMapping("/list")
    @ApiOperation(value = "获取巡检任务列表")
    public R<List<QtCheckTaskListVO>> getTaskList(@RequestBody QtCheckTaskListDTO dto) {
        return R.success(qtCheckTaskService.checkTaskList(dto));
    }

    @GetMapping("{id}")
    @ApiOperation(value = "获取任务详细")
    public R<QtCheckTaskDetailVO> getTaskDetail(@PathVariable Long id) {
        return R.success(qtCheckTaskService.taskDetail(id, false));
    }

    @GetMapping("/pc/{id}")
    @ApiOperation(value = "pc获取任务详细")
    public R<QtCheckTaskDetailVO> getPcTaskDetail(@PathVariable Long id) {
        return R.success(qtCheckTaskService.taskDetail(id, true));
    }

    @PutMapping("/status")
    @ApiOperation(value = "修改任务状态")
    @SysLog("修改任务状态")
    public R<Boolean> getTaskList(@RequestBody QtCheckTaskStatusDTO status) {
        return R.success(qtCheckTaskService.updateStatus(status));
    }

    @PutMapping("/values")
    @ApiOperation(value = "手机端给任务设置值")
    @SysLog("手机端给任务设置值")
    public R<Boolean> itemValue(@RequestBody QtCheckTaskSubmitDTO dto) {
        return R.success(qtCheckTaskService.itemValue(dto));
    }

    @PutMapping("/pc/values")
    @ApiOperation(value = "给任务设置值")
    @SysLog("pc端给任务设置值")
    public R<Boolean> pcItemValue(@RequestBody QtPcCheckTaskSubmitDTO dto) {
        return R.success(qtCheckTaskService.pcItemValue(dto));
    }

    @GetMapping("/count")
    @ApiOperation(value = "获取待检任务数量")
    public R<List<QtCheckTaskTypeCountVO>> countUndoTask() {
        return R.success(qtCheckTaskService.countTaskByType());
    }

    @PostMapping("/finish")
    @ApiOperation(value = "完成任务")
    @SysLog("完成任务")
    public R<Boolean> finished(@RequestBody QtCheckResultDTO dto) {
        return R.success(qtCheckTaskService.finished(dto));
    }

    @PostMapping("/retask")
    @ApiOperation(value = "创建复检任务")
    @SysLog("创建复检任务")
    public R<Long> retask(@RequestBody QtReTaskDTO dto) {
        return R.success(qtCheckTaskService.retask(dto));
    }

    @GetMapping("defect/{id}")
    @ApiOperation(value = "缺陷项回显")
    public R<List<QtCheckTaskDefectVO>> defects(@PathVariable Integer id) {
        return R.success(qtCheckPlanGroupItemService.findTaskDefect(id));
    }

    @PostMapping("/page")
    @ApiOperation(value = "检验方案列表")
    public R<Page<QtCheckTaskListVO>> pageList(@RequestBody QtCheckTaskListDTO dto) {
        return R.success(qtCheckTaskService.pageList(dto));
    }

    @GetMapping("/productOrder/code")
    @ApiOperation(value = "生产订单号查询生产车间产线批次")
    public R<QtProduceOrderLineVO> productOrderInfo(@RequestParam("code") String code) {
        return R.success(qtCheckTaskService.productOrderInfo(code));
    }

    @GetMapping ("/productOrder/by/{id}")
    @ApiOperation(value = "根据产线查询生产订单号")
    public R<QtProduceOrderLineVO> selectProductOrder(@PathVariable("id") Integer id) {
        return R.success(qtCheckTaskService.selectProductOrder(id));
    }

    @PostMapping("/productOrder/details/taskInfo")
    @ApiOperation(value = "根据生产订单号查询检验任务详情")
    public R<Page<QtCheckProduceTaskVO>> selectProductDetails(@RequestBody QtCheckaProductTaskListDTO dto) {
        return R.success(qtCheckTaskService.pageProductOrder(dto));
    }

    @PostMapping("/deep/acceptability/detail")
    @ApiOperation(value = "深加工合格率趋势明细表分页")
    public R<Page<QtDeepOrSubAcceptabilityDetailVO>> getQtDeepAcceptabilityDetail(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        return R.success(qtCheckTaskService.getQtDeepOrSubAcceptabilityDetailVO(QtManagerConstant.DEEP_OFFICE_NAME,dto));
    }

    @PostMapping("/sub/acceptability/detail")
    @ApiOperation(value = "基片合格率趋势明细表分页")
    public R<Page<QtDeepOrSubAcceptabilityDetailVO>> getQtSubAcceptabilityDetail(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        return R.success(qtCheckTaskService.getQtDeepOrSubAcceptabilityDetailVO(QtManagerConstant.SUB_OFFICE_NAME,dto));
    }

    @PostMapping("/deep/acceptability/chart")
    @ApiOperation(value = "深加工合格率趋势柱状图")
    public R<QtLineChartBiBarDTO> getQtChartDeepBarVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        return R.success(qtCheckTaskService.getQtChartDeepOrSubBarVO(QtManagerConstant.DEEP_OFFICE_NAME, dto));
    }

    @PostMapping("/sub/acceptability/chart")
    @ApiOperation(value = "基片合格率趋势柱状图")
    public R<QtLineChartBiBarDTO> getQtChartSubBarVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        return R.success(qtCheckTaskService.getQtChartDeepOrSubBarVO(QtManagerConstant.SUB_OFFICE_NAME, dto));
    }

    @PostMapping("/sub/spot/acceptability/chart")
    @ApiOperation(value = "基片化验抽检合格率趋势图表")
    public R<QtLineChartBiBarDTO> getQtSubLaboratoryChartVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode());
        return R.success(qtCheckTaskService.getQtChartDeepOrSubBarVO(QtManagerConstant.SUB_OFFICE_NAME, dto));
    }

    @PostMapping("/sub/chemical/acceptability/detail")
    @ApiOperation(value = "基片化验抽检合格率趋势明细分页")
    public R<Page<QtCheckTaskReportVO>> getQtSubLaboratoryAcceptabilityDetailVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode());
        return R.success(qtCheckTaskService.getQtSubLaboratoryAcceptabilityDetailVO(dto));
    }

    @PostMapping("/sub/chemical/acceptability/download")
    @ApiOperation(value = "基片化验抽检合格率趋势明细分页")
    public void downloadQtSubLaboratoryAcceptability(@RequestBody QtSubOrDeepAcceptabilityDTO dto,HttpServletResponse response) throws IOException{
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode());
        try{
            qtCheckTaskService.downloadQtSubLaboratoryAcceptabilityDetailVO(dto,response);
        } catch (Exception e) {
            // 若有异常则重置response
            this.exceptionResponse(response,e);
        }
    }

    @PostMapping("/accessory/acceptability/chart")
    @ApiOperation(value = "配料抽检合格率")
    public R<QtLineChartBiBarDTO> accessoryChart(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode());
        return R.success(qtCheckTaskService.accessoryChart(dto));
    }

    @PostMapping("/iqc/acceptability/rate/chart")
    @ApiOperation(value = "来料检合格率")
    public R<QtLineChartBiBarDTO> getQtIQCAcceptabilityChartVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode());
        return R.success(qtCheckTaskService.getQtIQCAcceptabilityChart(dto));
    }

    @PostMapping("/accessory/acceptability/detail")
    @ApiOperation(value = "配料抽检合格率明细分页")
    public R<Page<QtAccessoryAcceptabilityDetailVO>> getQtAccessoryAcceptabilityDetailVO(@RequestBody QtAccessoryAcceptabilityDetailDTO dto) {
        return R.success(qtCheckTaskService.getQtAccessoryAcceptabilityDetailVO(dto));
    }


//    @PostMapping("/accessory/acceptability/chart")
//    @ApiOperation(value = "配料抽检合格率柱状图")
//    public R<QtChartBarVO> getAccessoryChartDefectVO(@RequestBody QtAccessoryAcceptabilityDTO dto) {
//        return R.success(qtCheckTaskService.getAccessoryChartDefectVO(dto));

//    }

    @PostMapping("/iqc/acceptability/detail")
    @ApiOperation(value = "来料检合格率明细分页")
    public R<Page<QtIQCAcceptabilityDetailVO>> getQtIQCAcceptabilityDetailVO(@RequestBody QtSubOrDeepAcceptabilityDTO dto) {
        return R.success(qtCheckTaskService.getQtIQCAcceptabilityDetailVO(dto));
    }

    @PostMapping("/accessory/acceptability/download")
    @ApiOperation(value = "下载配料抽检合格率")
    public void downloadAccessoryAcceptability(@RequestBody AccessoryAcceptabilityDownDTO dto, HttpServletResponse response) throws IOException {
        try{
            qtCheckTaskService.downloadAccessoryAcceptability(dto,response);
        } catch (Exception e) {
            // 若有异常则重置response
            this.exceptionResponse(response,e);
        }
    }

    /**
     * 对响应异常进行封装
     * @param response
     * @throws IOException
     */
    protected void exceptionResponse(HttpServletResponse response ,Exception e) throws IOException{
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        Map<String, String> map = MapUtils.newHashMap();
        map.put("status", "failure");
        map.put("message", "下载文件失败" + e.getMessage());
        response.getWriter().println(JSON.toJSONString(map));
    }

}
