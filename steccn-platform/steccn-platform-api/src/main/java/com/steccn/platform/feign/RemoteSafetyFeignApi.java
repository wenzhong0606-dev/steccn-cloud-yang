package com.steccn.platform.feign;

import com.steccn.platform.dto.SafetyUserNameDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 安环接口
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@FeignClient(name = "remoteSafetyFeignApi", url = "${safety.url}", decode404 = true)
public interface RemoteSafetyFeignApi {
    /**
     * 根据分类id查询分类信息
     *
     * @param dto
     * @return
     */
    @PostMapping(value = "/srp-app/system/login/gogoNewNoLogin")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String getSafetyToken(@RequestBody SafetyUserNameDTO dto);
}
