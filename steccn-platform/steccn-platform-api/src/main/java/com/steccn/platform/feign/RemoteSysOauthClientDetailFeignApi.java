package com.steccn.platform.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

/**
 * @author yequan
 */
@FeignClient(contextId = "remoteSysOauthClientDetailFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSysOauthClientDetailFeignApi {

    /**
     * 获取所有客户端加密后的字符串
     * @return
     */
    @ApiOperation(value = "获取所有客户端加密后的字符串")
    @GetMapping("/feign/platform/sys/client/pwd")
    R<Set<String>> clientEncodePassword();

}
