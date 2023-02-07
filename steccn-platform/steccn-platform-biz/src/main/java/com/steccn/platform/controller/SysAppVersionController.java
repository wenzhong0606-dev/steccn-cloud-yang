package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysAppVersionDTO;
import com.steccn.platform.service.SysAppVersionService;
import com.steccn.platform.vo.SysAppVersionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * app版本控制器
 *
 * @author Mongknife
 * @date 2023/1/31
 */
@Api("app版本控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/app/version")
public class SysAppVersionController {

    private final SysAppVersionService sysAppVersionService;

    @ApiOperation("app版本分页列表")
    @PostMapping("/list")
    public R<Page<SysAppVersionVO>> pageList(@RequestBody SysAppVersionDTO dto) {
        return R.success(sysAppVersionService.pageList(dto));
    }

    @ApiOperation("app版本新增")
    @PostMapping()
    @SysLog("app版本新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody SysAppVersionDTO dto) {
        return R.success(sysAppVersionService.add(dto));
    }

    @ApiOperation("app最新版本获取")
    @GetMapping("/latest")
    public R<SysAppVersionVO> getLatestVersion() {
        return R.success(sysAppVersionService.getLatestVersion());
    }
}
