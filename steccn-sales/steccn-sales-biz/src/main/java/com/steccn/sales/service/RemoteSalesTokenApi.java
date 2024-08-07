package com.steccn.sales.service;

import com.steccn.common.constant.FeignConstant;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/08/07
 */
@FeignClient(contextId = "remoteSalesTokenApi", value = FeignConstant.STECCN_AUTH_SERVER)
public interface RemoteSalesTokenApi {


    /**
     * 获取token
     *
     * @param grantType 授权类型
     * @param password  密码
     * @param username  用户名
     * @param clientId  客户端id
     * @param scope     授权范围
     * @return
     */
    @PostMapping(value = "/oauth/token" , headers = {"Authorization=Basic YWRtaW46YWRtaW4="})
    @Headers(value = {"Authorization=Basic YWRtaW46YWRtaW4="})
    Object getToken(@RequestParam("grant_type") String grantType,
                    @RequestParam("password") String password,
                    @RequestParam("username") String username,
                    @RequestParam("client_id") String clientId,
                    @RequestParam("scope") String scope);

    @PostMapping(value = "/oauth/token" , headers = {"Authorization=Basic dGVtcDpzdGVjY25fdGVtcA=="})
    @Headers(value = {"Authorization=Basic dGVtcDpzdGVjY25fdGVtcA=="})
    Object getTempToken(@RequestParam("grant_type") String grantType,
                        @RequestParam("password") String password,
                        @RequestParam("username") String username,
                        @RequestParam("client_id") String clientId,
                        @RequestParam("scope") String scope);
}
