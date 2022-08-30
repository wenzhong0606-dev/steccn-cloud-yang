package com.steccn.authserver.service;

import com.steccn.authserver.dto.OaLoginParam;
import com.steccn.authserver.dto.OaSsoTokenParam;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@FeignClient(name = "oaService",url = "61.177.139.244:8887",decode404 = true)
public interface OaApi {

    /**
     * oa校验登录接口
     * @param loginParam
     * @return String
     */
    @PostMapping(value = "/api/hrm/login/checkLogin",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String checkLogin(@RequestBody OaLoginParam loginParam);

    /**
     * oa跳过登录页token
     * @param oaSSoTokenParam
     * @return
     */
    @PostMapping(value = "/ssologin/getToken",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String getSsoToken(@RequestBody OaSsoTokenParam oaSSoTokenParam);

    /**
     * 获取apiToken,请求oa接口用
     * @param headers 传参需要放入header中
     * @return
     */
    @PostMapping(value = "/api/ec/dev/auth/applytoken")
    @ResponseBody
    String getApiToken(@RequestHeader MultiValueMap<String,String> headers);
}
