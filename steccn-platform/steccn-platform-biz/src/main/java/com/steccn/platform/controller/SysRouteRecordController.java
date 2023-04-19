package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysRouteRecordDTO;
import com.steccn.platform.service.SysRouteRecordService;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 路由记录
 *
 * @author AndrewLuo
 * @date 2023/04/19
 */
@Api("路由记录")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/route")
public class SysRouteRecordController {
    private final SysRouteRecordService sysRouteRecordService;

    @ApiOperation("保存记录")
    @PostMapping("/save")
    public R<Boolean> saveRouteRecord(@RequestBody SysRouteRecordDTO dto) {
        return R.success(sysRouteRecordService.saveRecord(dto));
    }

    @ApiOperation("拿到记录记录")
    @GetMapping("/record")
    public R<List<SysRouteRecordDTO>> getRecord() {
        return R.success(sysRouteRecordService.getRouteRecord());
    }
}
