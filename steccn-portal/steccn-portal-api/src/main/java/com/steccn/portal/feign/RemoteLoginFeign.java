package com.steccn.portal.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteLoginFeign", value = FeignConstant.STECCN_PORTAL_SERVICE_NAME)
public interface RemoteLoginFeign {

    /**
     * 统一门户登录
     * @param username 用户名
     * @param password 密码
     * @return r
     */
    @GetMapping("/portal/login")
    R login(@RequestParam("username") String username, @RequestParam("password") String password);
}
