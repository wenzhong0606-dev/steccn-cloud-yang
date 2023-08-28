package com.steccn.platform.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SRM系统API
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@FeignClient(name = "remoteSrmFeignApi", url = "${srm.url}", decode404 = true)
public interface RemoteSrmFeignApi {
    /**
     * 获取token
     * @param loginCode 登录名
     * @param clientId 默认值
     * @param grantType 默认值
     * @return
     */
    @GetMapping(value = "/oauth/public/sso/access-token")
    @ResponseBody
    String getToken(@RequestParam("loginCode")String loginCode,@RequestParam("client_id")String clientId,@RequestParam("grant_type")String grantType);
}
