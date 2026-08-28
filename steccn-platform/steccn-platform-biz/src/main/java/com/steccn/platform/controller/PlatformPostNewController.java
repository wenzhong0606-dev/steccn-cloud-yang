package com.steccn.platform.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.PlatformPostDTO;
import com.steccn.platform.service.PlatformPostService;
import com.steccn.platform.vo.PlatformPostVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/31
 */
@RestController
@RequiredArgsConstructor
@DS("slave")
@RequestMapping("/platform/post/new")
public class PlatformPostNewController {
    private final PlatformPostService platformPostService;

    @PostMapping()
    @ApiOperation(value = "新增岗位")
    public R<Boolean> add(@Valid @RequestBody PlatformPostDTO dto) {
        return R.success(platformPostService.add(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改岗位")
    public R<Boolean> update(@Valid @RequestBody PlatformPostDTO dto) {
        return R.success(platformPostService.update(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除岗位")
    public R<Boolean> delete(@PathVariable("id")Long id) {
        return R.success(platformPostService.delete(id));
    }

    @PostMapping("/page")
    @ApiOperation(value = "岗位分页")
    public R<Page<PlatformPostVO>> pageList(@RequestBody PlatformPostDTO dto) {
        return R.success(platformPostService.pageList(dto));
    }
}
