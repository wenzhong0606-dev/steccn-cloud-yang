package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.domain.PlatformUserRoleDTO;
import com.steccn.platform.dto.OaProcessPushDTO;
import com.steccn.platform.dto.PlatformUserDTO;
import com.steccn.platform.service.PlatformUserRoleService;
import com.steccn.platform.service.PlatformUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Api("OA接口数据接收")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/user")
public class PlatformUserController {
    private final PlatformUserService platformUserService;
    private final PlatformUserRoleService platformUserRoleService;


    @PostMapping()
    @ApiOperation(value = "新增用户")
    @PreAuthorize("hasAuthority('platform:user:add')")
    public R<Boolean> add(@Valid @RequestBody PlatformUserDTO dto) {
        return R.success(platformUserService.add(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改用户")
    public R<Boolean> update(@Valid @RequestBody PlatformUserDTO dto) {
        return R.success(platformUserService.update(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户")
    public R<Boolean> update(@PathVariable("id")Long id) {
        return R.success(platformUserService.delete(id));
    }

    @PostMapping("/role")
    @ApiOperation(value = "绑定角色")
    public R<Boolean> bind(@Valid @RequestBody PlatformUserRoleDTO dto) {
        return R.success(platformUserRoleService.bind(dto));
    }
}
