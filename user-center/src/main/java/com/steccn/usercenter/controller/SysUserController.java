package com.steccn.usercenter.controller;

import com.steccn.common.vo.R;
import com.steccn.usercenter.domain.SysUser;
import com.steccn.usercenter.repository.UserRepository;
import com.steccn.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Security;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@Api(tags = "用户中心")
@RestController
@RequestMapping("/user")
public class SysUserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/id/{id}")
    public Mono<R> findUserById(@PathVariable Long id) {
        return userRepository.getUserById(id).map(sysUser -> R.success(sysUser));
    }

    @GetMapping("/name/{name}")
    public Mono<R> findUserByName(@PathVariable String name) {
        return userRepository.getUserByName(name).map(sysUser -> R.success(sysUser));
    }
    @ApiOperation("新增用户")
    @PostMapping("/add")
    public Mono<R> addUserByName(@RequestBody SysUser user){
        return userRepository.save(user).map(sysUser -> R.success(sysUser));
    }

    @GetMapping("/userinfo/id/{id}")
    @ApiOperation("查询用户信息")
    public Mono<R> getUserInfoById(@PathVariable Long id) {

        return userService.getUserInfoByUserId(id).map(userDTO -> R.success(userDTO));
    }

    @GetMapping("/userinfo/name/{name}")
    @ApiOperation("查询用户信息")
    public Mono<R> getUserInfoByName(@PathVariable String name) {
        return userService.getUserInfoByUsername(name).map(userDTO -> R.success(userDTO));
    }
}
