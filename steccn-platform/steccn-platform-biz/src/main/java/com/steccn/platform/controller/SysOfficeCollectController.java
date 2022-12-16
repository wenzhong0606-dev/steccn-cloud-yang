package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.*;
import com.steccn.platform.service.SysOfficeCollectService;
import com.steccn.platform.vo.SysStationWarehouseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api("采集点编码配置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/office/collect")
public class SysOfficeCollectController {
    private final SysOfficeCollectService sysOfficeCollectService;
    @PostMapping("/page")
    @ApiOperation("采集点编码配置列表")
    public R<Page<SysStationWarehouseVO>> pageList(@RequestBody SysOfficeCollectPageDTO dto) {
        return R.success(sysOfficeCollectService.pageList(dto));
    }

    @SysLog("新增采集点编码配置")
    @PostMapping
    @ApiOperation("新增采集点编码配置")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody SysOfficeCollectAddDTO dto) {
        return R.success(sysOfficeCollectService.add(dto));
    }

    @SysLog("编辑采集点编码配置")
    @PutMapping
    @ApiOperation("编辑采集点编码配置")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody SysOfficeCollectUpdateDTO dto) {
        return R.success(sysOfficeCollectService.edit(dto));
    }

    @SysLog("删除采集点编码配置")
    @DeleteMapping("/{id}")
    @ApiOperation("工位采集点编码配置")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(sysOfficeCollectService.removeById(id));
    }
}
