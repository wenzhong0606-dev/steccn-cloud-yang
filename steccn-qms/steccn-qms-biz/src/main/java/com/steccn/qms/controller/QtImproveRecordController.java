package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.domain.QtImproveRecord;
import com.steccn.qms.dto.QtImproveRecordDTO;
import com.steccn.qms.dto.QtImproveRecordPageDTO;
import com.steccn.qms.service.QtImproveRecordService;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.qms.vo.QtImproveRecordVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public R<Boolean> add(@RequestBody QtImproveRecordDTO dto){
        return R.success(qtImproveRecordService.add(dto));
    }



}
