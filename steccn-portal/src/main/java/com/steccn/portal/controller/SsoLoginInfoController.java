package com.steccn.portal.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc 单点登录控制器
 */
@Api(tags = "菜单权限控制器")
@RestController
@RequestMapping("/portal/sso")
@Slf4j
public class SsoLoginInfoController {
}
