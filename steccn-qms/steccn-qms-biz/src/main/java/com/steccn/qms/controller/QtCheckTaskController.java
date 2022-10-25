package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public R<List<QtCheckTaskListVO>> getTaskList(@RequestBody QtCheckTaskListDTO dto){
        return R.success(qtCheckTaskService.checkTaskList(dto));
    }

    @GetMapping("{id}")
    @ApiOperation(value = "获取任务详细")
    public R<QtCheckTaskDetailVO> getTaskDetail(@PathVariable Long id){
        return R.success(qtCheckTaskService.taskDetail(id,false));
    }

    @GetMapping("/pc/{id}")
    @ApiOperation(value = "pc获取任务详细")
    public R<QtCheckTaskDetailVO> getPcTaskDetail(@PathVariable Long id){
        return R.success(qtCheckTaskService.taskDetail(id,true));
    }

    @PutMapping("/status")
    @ApiOperation(value = "修改任务状态")
    @SysLog("修改任务状态")
    public R<Boolean> getTaskList(@RequestBody QtCheckTaskStatusDTO status){
        return R.success(qtCheckTaskService.updateStatus(status));
    }

    @PutMapping("/values")
    @ApiOperation(value = "手机端给任务设置值")
    @SysLog("手机端给任务设置值")
    public R<Boolean> itemValue(@RequestBody QtCheckTaskSubmitDTO dto){
        return R.success(qtCheckTaskService.itemValue(dto));
    }

    @PutMapping("/pc/values")
    @ApiOperation(value = "给任务设置值")
    @SysLog("pc端给任务设置值")
    public R<Boolean> pcItemValue(@RequestBody QtPcCheckTaskSubmitDTO dto){
        return R.success(qtCheckTaskService.pcItemValue(dto));
    }

    @GetMapping("/count")
    @ApiOperation(value = "获取待检任务数量")
    public R<List<QtCheckTaskTypeCountVO>> countUndoTask(){
        return R.success(qtCheckTaskService.countTaskByType());
    }

    @PostMapping("/finish")
    @ApiOperation(value = "完成任务")
    @SysLog("完成任务")
    public R<Boolean> finished(@RequestBody QtCheckResultDTO dto){
        return R.success(qtCheckTaskService.finished(dto));
    }

    @GetMapping("retask/{id}")
    @ApiOperation(value = "创建复检任务")
    @SysLog("创建复检任务")
    public R<Long> retask(@PathVariable Long id){
        return R.success(qtCheckTaskService.retask(id));
    }

    @GetMapping("defect/{id}")
    @ApiOperation(value = "缺陷项回显")
    public R<List<QtCheckTaskDefectVO>> defects(@PathVariable Integer id){
        return R.success(qtCheckPlanGroupItemService.findTaskDefect(id));
    }

    @PostMapping("/page")
    @ApiOperation(value = "检验方案列表")
    public R<Page<QtCheckTaskListVO>> pageList(@RequestBody QtCheckTaskListDTO dto) {
        return R.success(qtCheckTaskService.pageList(dto));
    }
}
