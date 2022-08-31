package com.steccn.oa.service;

import com.alibaba.fastjson.JSONObject;
import com.steccn.oa.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import weaver.rsa.security.RSA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * oa系统实例
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OaService {

    private final OaApi oaApi;

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
     * @param oaSSoTokenParam
     * @return OaSsoTokenResult
     */
    public OaSsoTokenResult getSsoToken(OaSsoTokenParam oaSSoTokenParam){
        String result = oaApi.getSsoToken(oaSSoTokenParam);
        String res = result.substring(0,9);
        OaSsoTokenResult oaSsoTokenResult = new OaSsoTokenResult();
        if(res.equals("Token获取失败")){
            oaSsoTokenResult.setStatus(false);
        }else {
            oaSsoTokenResult.setStatus(true);
        }
        oaSsoTokenResult.setToken(result);
        return oaSsoTokenResult;
    }

    /**
     * 获取请求oa后台apiToken
     * @return OaApiTokenResult
     */
    public OaApiTokenResult getApiToken(){
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("appid","fad36a4ac8412767c11007fc1736cff5");
        headers.set("secret","wbSRHWiPyCMBY3NNkcsDHJVXjCe6y1NJglmzNeNlAgOTpqxbhh7Xe3ib2X2zm3Fq8kySb7guECI1ykYoew3DmHYvf6tAiWxrUPdAx7Cuzoo9NDW+cwTPuYHTH5njvSNY6NRnopr4QXHoAk2UY//I8X0l9v3Uz3CB6IkPz2mByXYLQoSHC6p1CovDDVJXhJf90hh844G5pj+63fQZUzuSOyBLlGcQ/DqYceHgtE5RCuWiDz1wsX7CehoRKqrmfkyguiiKCOhF5KYOpxpvys/1MI34IXh4t9UO06MfOrT9gCjlVKITql3Z6SHdqSv981DXgERCUmP30HHxLB8oPpvsrQ==");
        String result = oaApi.getApiToken(headers);
        JSONObject jsonObject = JSONObject.parseObject(result);
        OaApiTokenResult oaApiTokenResult = new OaApiTokenResult();
        if(jsonObject.getBoolean("status")){
            oaApiTokenResult.setToken(jsonObject.getString("token"));
        }
        oaApiTokenResult.setStatus(jsonObject.getBoolean("status"));
        oaApiTokenResult.setMsg(jsonObject.getString("msg"));
        return oaApiTokenResult;
    }

    /**
     * 获取oa菜单请求
     * @param userId
     * @return List<OaMenuResult>
     */
    public List<OaMenuResult> getMenuList(String userId){
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        //先获取apiToken
        OaApiTokenResult oaApiTokenResult = this.getApiToken();;
        if(!oaApiTokenResult.isStatus()){
            OaMenuResult oaMenuResult = new OaMenuResult();
            oaMenuResult.setStatus(false);
            oaMenuResults.add(oaMenuResult);
            log.error(oaApiTokenResult.getMsg());
            return oaMenuResults;
        }else{
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            //放入Token
            headers.set("appid","fad36a4ac8412767c11007fc1736cff5");
            headers.set("token",oaApiTokenResult.getToken());
            //根据泛微加密算法加入rsa
            RSA rsa = new RSA();
            //注册进系统时已生成
            String spk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4vp8JgKbgJ5r6fp1nS48hl/UFUv1OVcP9pAlxsMrvdELjglzsQqArKchASjJR3JblIjZBQiFwdt9v4wufj3jyoGRMdKEsxIzdSIvVuEjcvTDoyZeIeOUZPqlGyln6Me4ddekqtB9SK9HOmiwVuJYiajF3Do5zWcY1MBp+wUyH63KyjOKnwLv2+SkTexYsjEcEc585TCpUnxJIRfEN1ckY8s9y8oUVTlXhFFU9z/SAOADDmv2D0bYSOiKBRy/OjUZNbU9OMY0ErKt9epzXvccgGyTUpAG9mQohN55OSzjVmSW9sT8fgxGO96TphJRwmBDpW3q1A4ELHJza99+0RmZ8QIDAQAB";
            //用户id加密
            String userIdSecret = rsa.encrypt(null, userId, null, "utf-8", spk, false);
            headers.set("userid",userIdSecret);
            String result = oaApi.getOaMenu(headers);
            JSONObject jsonObject = JSONObject.parseObject(result);
            //递归拿到树状数据
            if(jsonObject.getJSONObject("data")!=null && !jsonObject.getJSONArray("data").isEmpty()){
                for(int i = 0; i < jsonObject.getJSONArray("data").size(); i++){
                    oaMenuResults.add(this.transferJsonToObject(jsonObject.getJSONArray("data").getJSONObject(i)));
                }
            }
            return oaMenuResults;
        }

    }

    /**
     * 递归获取菜单数据
     * @param jsonObject
     * @return
     */
    public OaMenuResult transferJsonToObject(JSONObject jsonObject){
        OaMenuResult oaMenuResult = new OaMenuResult();
        if(jsonObject.getJSONObject("child")==null && jsonObject.getJSONArray("child").isEmpty()){
            OaMenuResult tmp = new OaMenuResult();
            tmp.setPid(jsonObject.getIntValue("pid"));
            tmp.setId(jsonObject.getIntValue("id"));
            tmp.setName(jsonObject.getString("name"));
            tmp.setStatus(true);
            if(jsonObject.getJSONObject("url")!=null){
                tmp.setUrl(jsonObject.getString("url"));
            }
            return tmp;
        }
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        for(int i = 0; i <jsonObject.getJSONArray("child").size(); i++){
            oaMenuResults.add(transferJsonToObject(jsonObject.getJSONArray("child").getJSONObject(i)));
        }
        oaMenuResult.setPid(jsonObject.getIntValue("pid"));
        oaMenuResult.setId(jsonObject.getIntValue("id"));
        oaMenuResult.setName(jsonObject.getString("name"));
        oaMenuResult.setStatus(true);
        if(jsonObject.getJSONObject("url")!=null){
            oaMenuResult.setUrl(jsonObject.getString("url"));
        }
        oaMenuResult.setChild(oaMenuResults);
        return oaMenuResult;
    }

}
