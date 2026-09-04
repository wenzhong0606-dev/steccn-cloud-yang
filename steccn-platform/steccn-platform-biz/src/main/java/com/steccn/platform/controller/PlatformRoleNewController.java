package com.steccn.platform.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.PlatformRoleDTO;
import com.steccn.platform.dto.PlatformRoleMenuDTO;
import com.steccn.platform.service.PlatformRoleMenuService;
import com.steccn.platform.service.PlatformRoleService;
import com.steccn.platform.vo.PlatformRoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Api("OA接口数据接收")
@RestController
@RequiredArgsConstructor
@DS("slave")
@RequestMapping("/platform/new/role")
public class PlatformRoleNewController {
    private final PlatformRoleService platformRoleService;
    private final PlatformRoleMenuService platformRoleMenuService;

    @PostMapping()
    @ApiOperation(value = "新增角色")
    @PreAuthorize("hasAuthority('platform:role:add')")
    public R<Boolean> add(@Valid @RequestBody PlatformRoleDTO dto) {
        return R.success(platformRoleService.add(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改角色")
    @PreAuthorize("hasAuthority('platform:role:update')")
    public R<Boolean> update(@Valid @RequestBody PlatformRoleDTO dto) {
        return R.success(platformRoleService.update(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户")
    @PreAuthorize("hasAuthority('platform:role:update')")
    public R<Boolean> update(@PathVariable("id")Long id) {
        return R.success(platformRoleService.delete(id));
    }

    @PostMapping("/page")
    @ApiOperation(value = "角色分页")
    @PreAuthorize("hasAuthority('platform:role:all')")
    public R<Page<PlatformRoleVO>> pageList(@RequestBody PlatformRoleDTO dto) {
        return R.success(platformRoleService.pageList(dto));
    }

    @PostMapping("/bind")
    @ApiOperation(value = "绑定角色分页")
    public R<List<PlatformRoleVO>> bindPageList(@RequestBody PlatformRoleDTO dto) {
        return R.success(platformRoleService.bindPageList(dto));
    }

    @PostMapping("/menu")
    @ApiOperation(value = "绑定菜单")
    public R<Boolean> bind(@Valid @RequestBody PlatformRoleMenuDTO dto) {
        return R.success(platformRoleMenuService.bind(dto));
    }

}
