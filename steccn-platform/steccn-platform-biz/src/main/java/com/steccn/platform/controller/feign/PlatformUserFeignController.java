package com.steccn.platform.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.vo.PlatformUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Api("用户feign")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/user")
public class PlatformUserFeignController {
    private final PlatformUserService platformUserService;

    @ApiOperation(value = "获取用户信息")
    @GetMapping("/userName")
    public R<PlatformUserVO> userInfoByUserName(@RequestParam("userName") String userName) {
        return R.success(platformUserService.getByUserName(userName));
    }
}
