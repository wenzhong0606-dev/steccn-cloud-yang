package com.steccn.portal.controller;

import com.steccn.common.vo.R;
import com.steccn.portal.service.PermissionInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @desc 菜单权限控制器
 */
@Api(tags = "菜单权限控制器")
@RestController
@RequestMapping("/portal/permission")
@Slf4j
@RequiredArgsConstructor
public class PermissionInfoController {

    private final PermissionInfoService permissionInfoService;

    @ApiOperation("根据菜单code获取菜单")
    @GetMapping("/{code}")
    public Mono<R> getPermissionByCode(@PathVariable("code") String code) {
        return permissionInfoService.getPermissionByCode(code).map(optional ->{
                    if(optional.isPresent())
                        return R.success(optional.get());
                    else
                        return R.failure("您无此代码代表的权限");
                });
   }
}
