package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtImproveRecordService;
import com.steccn.qms.vo.QtImproveRecordVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/improve/record")
@ApiModel("整改记录控制器")
@Slf4j
public class QtImproveRecordController {

    private final QtImproveRecordService qtImproveRecordService;


    @ApiOperation(value = "整改记录列表")
    @PostMapping("/list")
    public R<Page<QtImproveRecordVO>> list(@RequestBody QtImproveRecordPageDTO dto) {
        return R.success(qtImproveRecordService.listPage(dto));
    }


    @ApiOperation(value = "新建整改记录")
    @PostMapping
    @SysLog("新建整改记录")
    public R<Boolean> add(@Validated(InsertGroup.class)@RequestBody QtImproveRecordDTO dto) {
        return R.success(qtImproveRecordService.add(dto));
    }


    @ApiOperation(value = "编辑整改记录")
    @PutMapping
    @SysLog("编辑整改记录")
    public R<Boolean> edit(@RequestBody QtImproveRecordDTO dto) {
        return R.success(qtImproveRecordService.edit(dto));
    }


    @ApiOperation(value = "处理整改记录")
    @PostMapping("/dispose")
    @SysLog("处理整改记录")
    public R<Boolean> dispose(@RequestBody QtImproveRecordDisposeDTO dto) {
        return R.success(qtImproveRecordService.dispose(dto));
    }


    @ApiOperation(value = "退回整改记录")
    @PostMapping("/send/back")
    @SysLog("退回整改记录")
    public R<Boolean> sendBack(@RequestBody QtImproveRecordSendBackDTO dto) {
        return R.success(qtImproveRecordService.sendBack(dto));
    }

    @ApiOperation(value = "获取整改记录详情")
    @GetMapping("/{id}")
    public R<QtImproveRecordVO> get(@PathVariable Long id) {
        return R.success(qtImproveRecordService.get(id));
    }

    @ApiOperation(value = "整改记录中断")
    @PostMapping("/suspend")
    public R<Boolean> suspend(@RequestBody QtImproveRecordSuspendDTO dto){
        return R.success(qtImproveRecordService.suspend(dto));
    }


    @ApiOperation(value = "提交审批")
    @PostMapping("/approve")
    public R<Boolean> approve(@RequestBody QtImproveRecordApproveDTO dto){
        return R.success(qtImproveRecordService.approve(dto));
    }


    @ApiOperation(value = "删除整改记录")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id){
        return R.success(qtImproveRecordService.delete(id));
    }

}
