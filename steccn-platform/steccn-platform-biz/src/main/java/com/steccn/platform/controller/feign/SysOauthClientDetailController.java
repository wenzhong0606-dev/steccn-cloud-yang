package com.steccn.platform.controller.feign;

import com.steccn.common.constant.CacheConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysOauthClientDetailService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 客户端凭证接入
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/client")
public class SysOauthClientDetailController {

    private final SysOauthClientDetailService sysOauthClientDetailService;

    @ApiOperation(value = "获取所有客户端加密后的字符串")
    @GetMapping("/pwd")
    public R<List<String>> clientEncodePassword() {
        return R.success(sysOauthClientDetailService.clientEncodePassword());
    }
}
