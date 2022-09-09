package com.steccn.qms.controller;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.QtCheckTaskDetailVO;
import com.steccn.qms.vo.QtCheckTaskListVO;
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

    @PostMapping
    @ApiOperation(value = "新建巡检任务")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtAddCheckTaskDTO dto) {
        return R.success(qtCheckTaskService.add(dto));
    }

    @PostMapping("/list")
    public R<List<QtCheckTaskListVO>> getTaskList(@RequestBody QtCheckTaskListDTO dto){
        return R.success(qtCheckTaskService.checkTaskList(dto));
    }

    @GetMapping("{id}")
    public R<QtCheckTaskDetailVO> getTaskList(@PathVariable Long id){
        return R.success(qtCheckTaskService.taskDetail(id));
    }

    @PutMapping("/status")
    public R<Boolean> getTaskList(@RequestBody QtCheckTaskStatusDTO status){
        return R.success(qtCheckTaskService.updateStatus(status));
    }

    @PutMapping("/values")
    public R<Boolean> itemValue(@RequestBody QtCheckTaskSubmitDTO dto){
        return R.success(qtCheckTaskService.itemValue(dto));
    }

    @PostMapping("/finish")
    public R<Boolean> finished(@RequestBody QtCheckResultDTO dto){
        return R.success(qtCheckTaskService.finished(dto));
    }

    @GetMapping("retask/{id}")
    public R<Long> finished(@PathVariable Long id){
        return R.success(qtCheckTaskService.retask(id));
    }
}
