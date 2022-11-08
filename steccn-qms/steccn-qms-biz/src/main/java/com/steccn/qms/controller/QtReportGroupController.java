package com.steccn.qms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtReportGroupDTO;
import com.steccn.qms.service.QtReportGroupService;
import com.steccn.qms.vo.QtReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SPC控制图类型
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/reportgroup")
@Slf4j
public class QtReportGroupController {
    private final QtReportGroupService qtReportGroupService;

    @PostMapping
    @ApiOperation(value = "新建报表卡片分组")
    @SysLog("新建报表卡片类型任务")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtReportGroupDTO dto) {
        return R.success(qtReportGroupService.add(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据报表卡片分组ID去查询报表卡片")
    public R<List<QtReportVO>> getQtReportList(@PathVariable("id") Integer id){
        return R.success(qtReportGroupService.getQtReportList(id));
    }
}
