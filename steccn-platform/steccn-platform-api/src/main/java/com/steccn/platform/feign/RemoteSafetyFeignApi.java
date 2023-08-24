package com.steccn.platform.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.dto.SafetyUserNameDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    /**
     * 获取安环系统待办列表
     * @param headers obj空对象
     * @return
     */
    @PostMapping(value = "/srp-app/md/mdMainPageStatistic/findThirdSystemTotal")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String getSafetyTodoList(@RequestHeader MultiValueMap<String,String> headers,@RequestBody TupleDTO dto);
}
