package com.steccn.qms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.QtReportService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/report")
@Slf4j
public class QtReportController {
    private final QtReportService qtReportService;

    @PostMapping
    @ApiOperation(value = "新建报表任务")
    @SysLog("新建报表任务")
    public R<Integer> add(@Validated(InsertGroup.class) @RequestBody QtAddReportDTO dto) {
        return R.success(qtReportService.add(dto));
    }
}
