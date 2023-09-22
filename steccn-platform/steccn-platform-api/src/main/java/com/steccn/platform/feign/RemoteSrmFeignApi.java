package com.steccn.platform.feign;

import com.steccn.platform.dto.SrmOaWorkFlowDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取请求api的token
     * @return
     */
    @PostMapping(value = "/oauth/oauth/token?grant_type=client_credentials&client_id=oa&client_secret=secret")
    @ResponseBody
    String getOauthToken();

    /**
     * 推送oa消息给SRM
     * @param headers
     * @return
     */
    @PostMapping(value = "/hitf/v1/rest/invoke?namespace=SRM&serverCode=OA_TO_SRM&interfaceCode=srm-interface.cux-oa-to-srm.oaCallback")
    @ResponseBody
    String sendOaWorkFlowMsg(@RequestHeader MultiValueMap<String,String> headers, @RequestBody SrmOaWorkFlowDTO dto);

    /**
     * 获取销售系统收货单
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/hitf/v1/rest/invoke?namespace=SRM&serverCode=DELIVERY_NOTE_INTERFACE&interfaceCode=srm-interface.asn-export.pull")
    @ResponseBody
    String querySalesOrder(@RequestHeader MultiValueMap<String,String> headers, @RequestBody SrmOaWorkFlowDTO dto);

}
