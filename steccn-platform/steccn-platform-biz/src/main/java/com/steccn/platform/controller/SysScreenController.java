package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysScreenConfigDTO;
import com.steccn.platform.service.SysScreenConfigDetailService;
import com.steccn.platform.service.SysScreenConfigService;
import com.steccn.platform.vo.SysScreenConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 屏幕配置控制器
 *
 * @author AndrewLuo
 * @date 2024/04/22
 */
@Api("屏幕配置控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/screen")
public class SysScreenController {
    private final SysScreenConfigService sysScreenConfigService;
    private final SysScreenConfigDetailService sysScreenConfigDetailService;

    @ApiOperation("新增屏幕配置")
    @PostMapping()
    public R<Boolean> add(@RequestBody SysScreenConfigDTO dto) {
        return R.success(sysScreenConfigService.add(dto));
    }

    @ApiOperation("新增屏幕配置")
    @PostMapping("list")
    public R<Page<SysScreenConfigVO>> pageList(@RequestBody BasePageDTO dto) {
        return R.success(sysScreenConfigService.pageList(dto));
    }

    @ApiOperation("修改屏幕配置")
    @PutMapping()
    public R<Boolean> updateData(@RequestBody SysScreenConfigDTO dto) {
        return R.success(sysScreenConfigService.updateData(dto));
    }

    @ApiOperation("删除屏幕配置")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteData(@PathVariable("id") Integer id) {
        return R.success(sysScreenConfigService.deleteData(id));
    }

    @ApiOperation("获取屏幕配置详情")
    @GetMapping("/{id}")
    public R<SysScreenConfigVO> info(@PathVariable("id") Integer id) {
        return R.success(sysScreenConfigService.info(id));
    }
}
