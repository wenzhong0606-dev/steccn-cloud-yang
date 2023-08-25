package com.steccn.platform.controller;

import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SafetyTodoListService;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.vo.SafetyTodoListVO;
import com.steccn.platform.vo.SafetyTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    private final SafetyTodoListService safetyTodoListService;

    @GetMapping("/token")
    @ApiOperation(value = "获取安环系统token")
    @ResponseBody
    public R<SafetyTokenVO> getToken() {
        return R.success(safetyTokenService.getSafetyToken(SecurityUtils.getUser().getUserId()));
    }

    @GetMapping("/removeToken")
    @ApiOperation(value = "删除安环系统token缓存")
    @ResponseBody
    public R<String> removeToken() {
        return R.success(safetyTokenService.removeSecurityToken());
    }

    @GetMapping("/todoList")
    @ApiOperation(value = "获取安环系统待办")
    @ResponseBody
    public R<List<SafetyTodoListVO>> getTodoList() {
        return R.success(safetyTodoListService.getTodoList());
    }
}
