package com.steccn.portal.controller;

import com.steccn.common.vo.R;
import com.steccn.portal.dto.PermissionInfoDTO;
import com.steccn.portal.dto.oa.OaLoginParam;
import com.steccn.portal.repository.PermissonInfoRepository;
import com.steccn.portal.service.OaService;
import com.steccn.portal.service.PermissionInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    private final OaService oaService;
//    @ApiOperation("根据菜单code获取菜单")
//    @GetMapping("/{code}")
//    public Mono<R<PermissionInfoDTO>> getPermissionByCode(@PathVariable("code") String code) {
//        return permissionInfoService.getPermissionByCode(code).map(v -> R.success(v));
//    }

    @PostMapping("/test")
    public R getOaData(@RequestBody OaLoginParam oaLoginParam){
        return R.success(oaService.checkLogin(oaLoginParam));
    }
}
