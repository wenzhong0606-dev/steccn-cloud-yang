package com.steccn.platform.controller.feign;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.common.constant.CacheConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysOauthClientDetail;
import com.steccn.platform.dto.SysLogDTO;
import com.steccn.platform.service.SysOauthClientDetailService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

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
    @Cacheable(value = CacheConstant.SYS_CLIENT_ENCODE_PASSWORD)
    public R<Set<String>> clientEncodePassword() {
        return R.success(sysOauthClientDetailService.clientEncodePassword());
    }
}
