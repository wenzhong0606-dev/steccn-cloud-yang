package com.steccn.authserver.service;

import com.alibaba.fastjson.JSONObject;
import com.steccn.authserver.dto.OaLoginParam;
import com.steccn.authserver.dto.OaLoginResult;
import com.steccn.authserver.dto.OaSSoParam;
import com.steccn.authserver.dto.OaSSoResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * oa系统实例
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@Service
@Slf4j
public class OaService {
    @Autowired
    private OaApi oaApi;

    /**
     * 防止因为webflux产生的返回值报错
     * @param converters
     * @return
     */

    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }

    /**
     * 泛微登录校验
     * @param loginParam
     * @return OaLoginResult
     */
    public OaLoginResult checkLogin(OaLoginParam loginParam){
        String result = oaApi.checkLogin(loginParam);
        JSONObject loginObj = JSONObject.parseObject(result);
        OaLoginResult oaLoginResult = new OaLoginResult();
        oaLoginResult.setLoginStatus(loginObj.getBoolean("loginstatus"));
        if(loginObj.getInteger("userid")!=null){
            oaLoginResult.setUserId(loginObj.getIntValue("userid"));
        }
        oaLoginResult.setMsgCode(loginObj.getIntValue("msgcode"));
        oaLoginResult.setMsg(loginObj.getString("msg"));
        return oaLoginResult;
    }

    /**
     * 获得oa跳过登录token
     * @param oaSSoParam
     * @return
     */
    public OaSSoResult getSsoToken(OaSSoParam oaSSoParam){
        String result = oaApi.getSsoToken(oaSSoParam);
        //json转对象
        JSONObject ssoObj = JSONObject.parseObject(result);
        OaSSoResult oaSSoResult = new OaSSoResult();
        oaSSoResult.setMsg(ssoObj.getString("msg"));
        oaSSoResult.setCode(ssoObj.getIntValue("code"));
        oaSSoResult.setStatus(ssoObj.getBoolean("status"));
        if(ssoObj.getString("token")!=null){
            oaSSoResult.setToken(ssoObj.getString("token"));
        }
        return oaSSoResult;
    }
}
