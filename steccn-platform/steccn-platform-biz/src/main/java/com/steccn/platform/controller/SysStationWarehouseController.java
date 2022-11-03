package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysStationWarehouse;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.service.SysStationWarehouseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * @author chenhuan
 * @date 2022/11/2 16:45
 */
@Api("机构数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/station/warehouse")
public class SysStationWarehouseController {

    private final SysStationWarehouseService sysStationWarehouseService;

    @PostMapping("/page")
    @ApiOperation("工位配置列表")
    public R<Page<SysStationWarehouse>> pageList(@RequestBody SysStationWarehouseDTO dto) {
        return R.success(sysStationWarehouseService.pageList(dto));
    }

    @SysLog("新增工位配置")
    @PostMapping
    @ApiOperation("新增工位配置")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody SysStationWarehouseDTO dto) {
        return R.success(sysStationWarehouseService.add(dto));
    }

    @SysLog("编辑工位配置")
    @PutMapping
    @ApiOperation("编辑工位配置")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody SysStationWarehouseDTO dto) {
        return R.success(sysStationWarehouseService.edit(dto));
    }

    @SysLog("删除工位配置")
    @DeleteMapping("/{id}")
    @ApiOperation("工位配置删除")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(sysStationWarehouseService.removeById(id));
    }
}
