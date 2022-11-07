package com.steccn.qms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtCheckTaskDestoryInfo;
import com.steccn.qms.dto.QtAddCheckTaskDTO;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import com.steccn.qms.service.QtCheckTaskDestoryInfoService;
import com.steccn.qms.vo.QtCheckTaskDestoryInfoVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类说明  落板明细记录controller
 * @author
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/destoryInfo")
@Slf4j
public class QtCheckTaskDestoryInfoController {

    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;

    @PostMapping("/add")
    @ApiOperation(value = "新增落板记录")
    @SysLog("新增落板记录")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtCheckTaskDestoryInfoDTO dto) {
        return R.success(qtCheckTaskDestoryInfoService.add(dto));
    }

    @PostMapping("/addList")
    @ApiOperation(value = "新增落板记录列表")
    @SysLog("新增落板记录列表")
    public R<Boolean> addList(@Validated(InsertGroup.class) @RequestBody List<QtCheckTaskDestoryInfoDTO> dtoList) {
        return R.success(qtCheckTaskDestoryInfoService.addList(dtoList));
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除落板记录")
    @SysLog("删除落板记录")
    public R<Boolean> delete(@Validated({UpdateGroup.class}) @RequestBody QtCheckTaskDestoryInfoDTO dto) {
        return R.success(qtCheckTaskDestoryInfoService.delete(dto));
    }

    @PostMapping("/select")
    @ApiOperation(value = "查询落板记录")
    @SysLog("查询落板记录")
    public R<List<QtCheckTaskDestoryInfoVO>> select(@RequestBody QtCheckTaskDestoryInfoDTO dto) {
        return R.success(qtCheckTaskDestoryInfoService.select(dto));
    }






}
