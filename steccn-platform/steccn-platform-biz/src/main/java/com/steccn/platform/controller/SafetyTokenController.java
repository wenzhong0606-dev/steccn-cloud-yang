package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.vo.SafetyTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安环系统拦截器
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Api("安环控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/safety")
public class SafetyTokenController {
    private final SafetyTokenService safetyTokenService;

    @GetMapping("/token")
    @ApiOperation(value = "获取安环系统token")
    public R<SafetyTokenVO> getToken() {
        return R.success(safetyTokenService.getSafetyToken());
    }
}
