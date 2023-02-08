package com.steccn.portal.feign;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2023/2/8
 */
@FeignClient(name = "remotePortalMesFeignApi", url = "${mes.url}", decode404 = true)
public interface RemotePortalMesFeignApi {


    /**
     * 获取mes的sid
     * @param userName
     * @return
     */
    @GetMapping(value = "/web/auth/getSid")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String getSid(@RequestParam("userName")String userName);
}
