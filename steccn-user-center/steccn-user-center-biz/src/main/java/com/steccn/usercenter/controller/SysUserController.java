package com.steccn.usercenter.controller;

import com.steccn.common.util.SteccnUserBaseUtils;
import com.steccn.common.vo.R;
import com.steccn.common.vo.SteccnUserBaseInfo;
import com.steccn.usercenter.domain.SysUser;
import com.steccn.usercenter.dto.UserSaveDTO;
import com.steccn.usercenter.repository.UserRepository;
import com.steccn.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@Api(tags = "用户中心")
@RestController
@RequestMapping("/user")
@Slf4j
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
    public Mono<R> addUserByName(@RequestBody SysUser user) {
        return userRepository.save(user).map(sysUser -> R.success(sysUser));
    }

    @GetMapping("/userinfo/id/{id}")
    @ApiOperation("查询用户信息")
    public Mono<R> getUserInfoById(@PathVariable Long id) {

        return userService.getUserInfoByUserId(id).map(userDTO -> R.success(userDTO));
    }

    @GetMapping("/userinfo/name/{name}")
    @ApiOperation("查询用户信息")
    public Mono<R> getUserInfoByName(ServerHttpRequest request, @PathVariable String name) {
        // 获取相关对象
        SteccnUserBaseInfo userBaseInfo = SteccnUserBaseUtils.getUserBaseInfo(request);
        return userService.getUserInfoByUsername(name).map(userDTO -> R.success(userDTO));
    }

    @PostMapping("/userinfo/save")
    @ApiOperation("保存用户信息")
    public Mono<SysUser> saveUserInfo(@RequestBody UserSaveDTO userSaveDTO) {
        return userService.saveUserInfo(userSaveDTO);
    }
}
