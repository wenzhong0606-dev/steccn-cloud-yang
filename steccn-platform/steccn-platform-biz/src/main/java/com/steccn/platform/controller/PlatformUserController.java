package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.util.AESUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.PlatformUserRoleDTO;
import com.steccn.platform.dto.OaProcessPushDTO;
import com.steccn.platform.dto.PlatformUserDTO;
import com.steccn.platform.service.PlatformUserMacService;
import com.steccn.platform.service.PlatformUserRoleService;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.vo.PlatformUserVO;
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
    private final PlatformUserMacService platformUserMacService;


    @PostMapping()
    @ApiOperation(value = "新增用户")
    @PreAuthorize("hasAuthority('platform:user:add')")
    public R<Boolean> add(@Valid @RequestBody PlatformUserDTO dto) {
        return R.success(platformUserService.add(dto));
    }

    @PostMapping("/en")
    @ApiOperation(value = "获取密码加密")
    public R<String> enPass(@Valid @RequestBody PlatformUserDTO dto) {
        return R.success(AESUtils.encrypt(dto.getPassword()));
    }

    @PutMapping()
    @ApiOperation(value = "修改用户")
    public R<Boolean> update(@Valid @RequestBody PlatformUserDTO dto) {
        return R.success(platformUserService.update(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户")
    @PreAuthorize("hasAuthority('platform:user:delete')")
    public R<Boolean> update(@PathVariable("id")Long id) {
        return R.success(platformUserService.delete(id));
    }

    @PostMapping("/role")
    @ApiOperation(value = "绑定角色")
    public R<Boolean> bind(@Valid @RequestBody PlatformUserRoleDTO dto) {
        return R.success(platformUserRoleService.bind(dto));
    }

    @PostMapping("/pageList")
    @ApiOperation(value = "用户分页")
    @PreAuthorize("hasAuthority('platform:user:list')")
    public R<Page<PlatformUserVO>> pageList(@RequestBody PlatformUserDTO dto) {
        return R.success(platformUserService.pageList(dto));
    }

    @GetMapping("/clearMac/{id}")
    @ApiOperation(value = "清除用户mac地址")
    @PreAuthorize("hasAuthority('platform:user:clearMac')")
    public R<Boolean> clearMac(@PathVariable("id")Long userId) {
        return R.success(platformUserMacService.clearMac(userId));
    }


}
