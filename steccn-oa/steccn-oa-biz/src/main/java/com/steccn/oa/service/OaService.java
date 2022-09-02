package com.steccn.oa.service;

import com.alibaba.fastjson.JSONObject;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
        oaLoginResult.setLoginStatus(loginObj.getBoolean(OaConstant.OA_JSON_LOGIN_STATUS));
        if(loginObj.getInteger(OaConstant.OA_JSON_USER_ID)!=null){
            oaLoginResult.setUserId(loginObj.getIntValue(OaConstant.OA_JSON_USER_ID));
        }
        oaLoginResult.setMsgCode(loginObj.getIntValue(OaConstant.OA_JSON_MSG_CODE));
        oaLoginResult.setMsg(loginObj.getString(OaConstant.OA_JSON_MSG));
        return oaLoginResult;
    }

    /**
     * 获得oa跳过登录token
     * @param oaSSoTokenParam
     * @return OaSsoTokenResult
     */
    public OaSsoTokenResult getSsoToken(OaSsoTokenParam oaSSoTokenParam){
        oaSSoTokenParam.setAppid(OaConstant.OA_SSO_APPID);
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
        headers.set("appid",OaConstant.OA_API_APPID);
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
            headers.set("appid",OaConstant.OA_API_APPID);
            headers.set("token",oaApiTokenResult.getToken());
            //根据泛微加密算法加入rsa
            RSA rsa = new RSA();
            //注册进系统时已生成
            String spk = OaConstant.OA_SPK;
            //用户id加密
            String userIdSecret = rsa.encrypt(null, userId, null, "utf-8", spk, false);
            headers.set("userid",userIdSecret);
            String result = oaApi.getOaMenu(headers);
            JSONObject jsonObject = JSONObject.parseObject(result);
            //递归拿到树状数据
            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATA)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).isEmpty()){
                for(int i = 0; i < jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).size(); i++){
                    oaMenuResults.add(this.transferJsonToObject(jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).getJSONObject(i)));
                }
            }
            return oaMenuResults;
        }

    }

    /**
     * 递归获取菜单数据
     * @param jsonObject
     * @return OaMenuResult
     */
    public OaMenuResult transferJsonToObject(JSONObject jsonObject){
        OaMenuResult oaMenuResult = new OaMenuResult();
        if(jsonObject.getJSONArray(OaConstant.OA_JSON_CHILD)==null || jsonObject.getJSONArray(OaConstant.OA_JSON_CHILD).isEmpty()){
            OaMenuResult tmp = new OaMenuResult();
            tmp.setPid(jsonObject.getIntValue(OaConstant.OA_JSON_PID));
            tmp.setId(jsonObject.getIntValue(OaConstant.OA_JSON_ID));
            tmp.setName(jsonObject.getString(OaConstant.OA_JSON_NAME));
            tmp.setStatus(true);
            if(jsonObject.getString(OaConstant.OA_JSON_URL)!=null){
                tmp.setUrl(jsonObject.getString(OaConstant.OA_JSON_URL));
            }
            return tmp;
        }
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        for(int i = 0; i <jsonObject.getJSONArray(OaConstant.OA_JSON_CHILD).size(); i++){
            oaMenuResults.add(transferJsonToObject(jsonObject.getJSONArray(OaConstant.OA_JSON_CHILD).getJSONObject(i)));
        }
        oaMenuResult.setPid(jsonObject.getIntValue(OaConstant.OA_JSON_PID));
        oaMenuResult.setId(jsonObject.getIntValue(OaConstant.OA_JSON_ID));
        oaMenuResult.setName(jsonObject.getString(OaConstant.OA_JSON_NAME));
        oaMenuResult.setStatus(true);
        if(jsonObject.getString(OaConstant.OA_JSON_URL)!=null){
            oaMenuResult.setUrl(jsonObject.getString(OaConstant.OA_JSON_URL));
        }
        oaMenuResult.setChild(oaMenuResults);
        return oaMenuResult;
    }

    /**
     * 通过用户名获取oa用户信息
     * @param loginId
     * @return  OaUserInfo
     */
    public OaUserInfo getOaUserInfo(String loginId){
        OaUserInfo oaUserInfo = new OaUserInfo();
        //放入Token
        OaApiTokenResult oaApiTokenResult = this.getApiToken();;
        if(!oaApiTokenResult.isStatus()){
            oaUserInfo.setStatus(false);
            log.error(oaApiTokenResult.getMsg());
            return oaUserInfo;
        }else{
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            //放入Token
            headers.set("appid",OaConstant.OA_API_APPID);
            headers.set("token",oaApiTokenResult.getToken());
            headers.set("userid",OaConstant.OA_SYSTEM_USERID);
            OaUserParam oaUserParam = new OaUserParam();
            oaUserParam.setQ(loginId);
            String result = oaApi.getOaUserInfo(headers,oaUserParam);
            //转成对象
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).isEmpty()){
                oaUserInfo.setDepartmentName(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).getJSONObject(0).getString(OaConstant.OA_JSON_DEPARTMENTNAME));
                oaUserInfo.setName(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).getJSONObject(0).getString(OaConstant.OA_JSON_NAME));
                oaUserInfo.setHeadPortrait(OaConstant.OA_WEAVER_SITE+jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).getJSONObject(0).getString(OaConstant.OA_JSON_ICON));
                oaUserInfo.setStatus(true);
            }else {
                oaUserInfo.setStatus(false);
                log.error("没有获取userInfo");
            }
            return oaUserInfo;
        }
    }

}
