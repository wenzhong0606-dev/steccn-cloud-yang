package com.steccn.platform.feign;

import com.steccn.platform.dto.SalesUserNameDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     *
     * @param dto
     * @return
     */
    @GetMapping(value = "/oauth/public/sso/access-token")
    @ResponseBody
    String getToken();
}
