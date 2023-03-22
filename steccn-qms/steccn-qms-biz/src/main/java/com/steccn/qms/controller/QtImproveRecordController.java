package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.domain.QtImproveRecord;
import com.steccn.qms.dto.QtImproveRecordDTO;
import com.steccn.qms.dto.QtImproveRecordDisposeDTO;
import com.steccn.qms.dto.QtImproveRecordPageDTO;
import com.steccn.qms.dto.QtImproveRecordSendBackDTO;
import com.steccn.qms.service.QtImproveRecordService;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.qms.vo.QtImproveRecordVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public R<Page<QtImproveRecordVO>> list(@RequestBody QtImproveRecordPageDTO dto){
        return R.success(qtImproveRecordService.listPage(dto));
    }


    @ApiOperation(value = "新建整改记录")
    @PostMapping
    @SysLog("新建整改记录")
    public R<Boolean> add(@RequestBody QtImproveRecordDTO dto){
        return R.success(qtImproveRecordService.add(dto));
    }


    @ApiOperation(value = "编辑整改记录")
    @PutMapping
    @SysLog("编辑整改记录")
    public R<Boolean> edit(@RequestBody QtImproveRecordDTO dto){
        return R.success(qtImproveRecordService.edit(dto));
    }


    @ApiOperation(value = "处理整改记录")
    @PutMapping("/dispose")
    @SysLog("处理整改记录")
    public R<Boolean> dispose(@RequestBody QtImproveRecordDisposeDTO dto){
        return R.success(qtImproveRecordService.dispose(dto));
    }


    @ApiOperation(value = "退回整改记录")
    @PutMapping("/send/back")
    @SysLog("退回整改记录")
    public R<Boolean> sendBack(@RequestBody QtImproveRecordSendBackDTO dto){
        return R.success(qtImproveRecordService.sendBack(dto));
    }










}
