package com.steccn.oa.service;

import com.alibaba.fastjson.JSONObject;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.OaApiTokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/02/23
 */
@Service
@RequiredArgsConstructor
public class OaTokenService {
    private final OaApi oaApi;

    /**
     * 获取请求oa后台apiToken
     * @return OaApiTokenResult
     */
    @Cacheable(cacheNames = "OaToken#300",unless = "#result.status == false")
    public OaApiTokenResult getApiToken(){
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("appid", OaConstant.OA_API_APPID);
        headers.set("secret",OaConstant.OA_API_SECRET);
        String result = oaApi.getApiToken(headers);
        JSONObject jsonObject = JSONObject.parseObject(result);
        OaApiTokenResult oaApiTokenResult = new OaApiTokenResult();
        if(jsonObject.getBoolean(OaConstant.OA_JSON_STATUS)){
            oaApiTokenResult.setToken(jsonObject.getString(OaConstant.OA_JSON_TOKEN));
        }
        oaApiTokenResult.setStatus(jsonObject.getBoolean(OaConstant.OA_JSON_STATUS));
        oaApiTokenResult.setMsg(jsonObject.getString(OaConstant.OA_JSON_MSG));
        return oaApiTokenResult;
    }
}
