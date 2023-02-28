package com.steccn.platform.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysUserClientRefDTO;
import com.steccn.platform.service.SysUserClientRefService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yequan
 * @desc 用户绑定移动设备控制器
 */
@Api("用户绑定移动设备控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/user/client")
public class SysUserClientFeignController {

    private final SysUserClientRefService sysUserClientRefService;

    /**
     * 移动端登录时，绑定消息通知
     *
     * @param dto dto
     * @return 绑定
     */
    @ApiOperation(value = "移动端登录时，绑定消息通知")
    @PostMapping("/bind")
    R<Boolean> bind(@Valid @RequestBody SysUserClientRefDTO dto) {
        return R.success(sysUserClientRefService.bind(dto));
    }
}
