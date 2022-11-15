package com.steccn.qms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtReportGroup;
import com.steccn.qms.dto.QtReportGroupDTO;
import com.steccn.qms.service.QtReportGroupService;
import com.steccn.qms.vo.QtReportChartVO;
import com.steccn.qms.vo.QtReportGroupVO;
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

    @GetMapping("/select")
    @ApiOperation(value = "查询当前分组数据列表,没有数据则返回请新建分组")
    public R<List<QtReportGroupVO>> getReportGroupVOlist(){
        return R.success(qtReportGroupService.getReportGroupVOlist());
    }
    @GetMapping ("/{id}")
    @ApiOperation(value = "根据报表卡片分组ID去查询报表卡片图表")
    public R<List<QtReportChartVO>> getQtReportList(@PathVariable("id") Integer id){
        return R.success(qtReportGroupService.getQtReportList(id));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "根据id删除报表卡片分组以及该分组下的卡片")
    @SysLog("根据id删除Spc卡片分组任务")
    public R<Boolean> deletQtReportGroup(@PathVariable("id") Integer id){
        return R.success(qtReportGroupService.deletQtReportGroup(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑报表分组")
    @SysLog("编辑报表分组行为")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody QtReportGroupDTO dto){
        return R.success(qtReportGroupService.edit(dto));
    }
}
