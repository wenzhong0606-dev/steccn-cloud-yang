package com.steccn.oa.controller;

import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaLoginParam;
import com.steccn.oa.dto.OaSsoTokenParam;
import com.steccn.oa.dto.OaSsoTokenResult;

import com.steccn.oa.service.OaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * oa拦截器
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Api(tags = "泛微oa接口")
@RestController
@RequestMapping("/oa")
@Slf4j
@RequiredArgsConstructor
public class OaController {

    private final OaService oaService;

    @ApiOperation(value = "获取oa用户的菜单权限")
    @GetMapping("/menuList/{userId}")
    public R getOaMenuList(@PathVariable("userId") String userId) {
        return R.success(oaService.getMenuList(userId));
    }

    @ApiOperation(value = "校验oa中的用户名密码")
    @PostMapping("/checkLogin")
    public R checkLogin(@RequestBody OaLoginParam oaLoginParam){
        return R.success(oaService.checkLogin(oaLoginParam));
    }

    @ApiOperation(value = "获取oa跳过登录页token")
    @GetMapping("/ssoToken/{loginId}")
    public R getSsoToken(@PathVariable("loginId") String loginId) {
        OaSsoTokenParam param = new OaSsoTokenParam();
        param.setLoginid(loginId);
        OaSsoTokenResult oaSsoTokenResult = oaService.getSsoToken(param);
        if(oaSsoTokenResult.isStatus()){
            return R.success(oaSsoTokenResult);
        }else {
            return R.failure(oaSsoTokenResult.getToken());
        }
    }

}
