package com.steccn.portal.controller;

import com.steccn.common.vo.R;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.portal.service.LoginService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @desc 统一登录
 */
@Api(tags = "统一登录")
@RestController
@RequestMapping("/portal")
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 统一登录，获取OA ， MESS-JAVA的菜单和token
     *
     * @param username 用户名
     * @param password 密码
     * @return r
     */
    @GetMapping("/login")
    public R login(@RequestParam("username") String username, @RequestParam("password") String password) {
        return loginService.login(username,password);
    }
}
