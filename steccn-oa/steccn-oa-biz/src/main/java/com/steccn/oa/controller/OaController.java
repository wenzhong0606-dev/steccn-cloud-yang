package com.steccn.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.vo.R;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.*;

import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.service.OaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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

    private final HrmresourceService hrmresourceService;

    @ApiOperation(value = "获取oa用户的菜单权限")
    @GetMapping("/menuList/{userId}")
    public R getOaMenuList(@PathVariable("userId") String userId) {
        return R.success(oaService.getMenuList(userId));
    }

    @ApiOperation(value = "校验oa中的用户名密码")
    @PostMapping("/checkLogin")
    public R checkLogin(@RequestBody OaLoginParam oaLoginParam) {
        return R.success(oaService.checkLogin(oaLoginParam));
    }

    @ApiOperation(value = "获取oa跳过登录页token")
    @GetMapping("/ssoToken/{loginId}")
    public R getSsoToken(@PathVariable("loginId") String loginId) {
        OaSsoTokenParam param = new OaSsoTokenParam();
        param.setLoginid(loginId);
        OaSsoTokenResult oaSsoTokenResult = oaService.getSsoToken(param);
        if (oaSsoTokenResult.isStatus()) {
            return R.success(oaSsoTokenResult);
        } else {
            return R.failure(oaSsoTokenResult.getToken());
        }
    }

    @ApiOperation("根据用户名查询用户")
    @GetMapping("/username")
    R loadByUsername(@RequestParam("username") String username) {
        Hrmresource one = hrmresourceService.getOne(new LambdaQueryWrapper<Hrmresource>().eq(Hrmresource::getLoginid, username));
        OaUserDTO oaUserDTO = null;
        if (Objects.nonNull(one)) {
            oaUserDTO = new OaUserDTO();
            oaUserDTO.setUsername(one.getLoginid());
            oaUserDTO.setPassword(one.getPassword());
        }
        return R.success(oaUserDTO);
    }

    @GetMapping("/userInfo/{loginId}")
    R getUserInfo(@PathVariable String loginId){
        OaUserInfo oaUserInfo = oaService.getOaUserInfo(loginId);
        if(oaUserInfo.getStatus()){
            return R.success(oaUserInfo);
        }else {
            return R.failure("userInfo为空");
        }
    }
}
