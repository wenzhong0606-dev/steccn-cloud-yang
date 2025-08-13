package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.PlatformMenuDTO;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.dto.PlatformRoleDTO;
import com.steccn.platform.dto.PlatformRoleMenuDTO;
import com.steccn.platform.service.PlatformMenuService;
import com.steccn.platform.service.PlatformOfficeService;
import com.steccn.platform.vo.PlatformMenuVO;
import com.steccn.platform.vo.PlatformOfficeVO;
import com.steccn.platform.vo.PlatformRoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/08/05
 */
@Api("菜单接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/menu")
public class PlatformMenuController {
    private final PlatformMenuService platformMenuService;

    @PostMapping()
    @ApiOperation(value = "新增菜单")
    @PreAuthorize("hasAuthority('platform:menu:add')")
    public R<Boolean> add(@Valid @RequestBody PlatformMenuDTO dto) {
        return R.success(platformMenuService.add(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改菜单")
    @PreAuthorize("hasAuthority('platform:menu:update')")
    public R<Boolean> update(@Valid @RequestBody PlatformMenuDTO dto) {
        return R.success(platformMenuService.update(dto));
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除菜单")
    public R<Boolean> delete(@RequestBody Set<Integer> ids) {
        return R.success(platformMenuService.deleteById(ids));
    }

    @GetMapping()
    @ApiOperation(value = "所有树")
    @PreAuthorize("hasAuthority('platform:menu:all')")
    public R<List<PlatformMenuVO>> listData() {
        return R.success(platformMenuService.getTreeList());
    }

    @GetMapping("/user")
    @ApiOperation(value = "获取权限相关的树")
    public R<List<PlatformMenuVO>> listUserData() {
        return R.success(platformMenuService.getUserTreeList());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取该节点下的树")
    @PreAuthorize("hasAuthority('platform:menu:all')")
    public R<PlatformMenuVO> parent(@PathVariable("id") Integer id) {
        return R.success(platformMenuService.getTreeById(id));
    }

    @PostMapping("/bind")
    @ApiOperation(value = "绑定菜单分页")
    public R<List<PlatformMenuVO>> bindPageList(@RequestBody PlatformRoleMenuDTO dto) {
        return R.success(platformMenuService.getBindTreeList(dto));
    }
}
