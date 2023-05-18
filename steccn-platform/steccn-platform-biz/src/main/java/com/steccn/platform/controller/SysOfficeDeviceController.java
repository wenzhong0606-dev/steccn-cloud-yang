package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeCollectUpdateDTO;
import com.steccn.platform.dto.SysOfficeDeviceAddDTO;
import com.steccn.platform.dto.SysOfficeDevicePageDTO;
import com.steccn.platform.dto.SysOfficeDeviceUpdateDTO;
import com.steccn.platform.service.SysOfficeDeviceService;
import com.steccn.platform.vo.SysOfficeDeviceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api("工位配置设备")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/office/device")
public class SysOfficeDeviceController {

    private final SysOfficeDeviceService sysOfficeDeviceService;

    @PostMapping("/page")
    @ApiOperation("工位设备配置列表")
    public R<Page<SysOfficeDeviceVO>> pageList(@RequestBody SysOfficeDevicePageDTO dto) {
        return R.success(sysOfficeDeviceService.pageList(dto));
    }

    @SysLog("新增工位设备配置")
    @PostMapping
    @ApiOperation("新增工位设备配置")
    public R<Boolean> add(@Validated @RequestBody SysOfficeDeviceAddDTO dto) {
        return R.success(sysOfficeDeviceService.add(dto));
    }

    @SysLog("编辑工位设备配置")
    @PutMapping
    @ApiOperation("编辑工位设备配置")
    public R<Boolean> edit(@Validated @RequestBody SysOfficeDeviceUpdateDTO dto) {
        return R.success(sysOfficeDeviceService.edit(dto));
    }

    @SysLog("删除工位设备配置")
    @DeleteMapping("/{id}")
    @ApiOperation("删除工位设备配置")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(sysOfficeDeviceService.removeById(id));
    }
}
