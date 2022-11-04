package com.steccn.oa.service;

import com.steccn.oa.dto.*;
import feign.Headers;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@FeignClient(name = "oaApi",url = "61.177.139.244:8887",decode404 = true)
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

    /**
     * 获取菜单,请求oa接口用
     * @param headers 传参需要放入header中
     * @return
     */
    @GetMapping(value = "/api/portal/menu/getFrontEndMenu?isLoadAll=1")
    @ResponseBody
    String getOaMenu(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 调用oa
     * @param oaUserParam MultiValueMap
     * @return
     */
    @PostMapping(value = "/api/public/browser/complete/1",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String getOaUserInfo(@RequestHeader MultiValueMap<String,String> headers,@RequestBody OaUserParam oaUserParam);

    /**
     * 调用oa
     * @param headers
     * @return
     */
    @PostMapping(value = "/api/public/browser/complete/1",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String getAllOaUser(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取OA公司下级数据
     * @param headers 传参需要放入header中
     * @return
     */
    @GetMapping(value = "/api/hrm/base/getHrmSearchTree")
    @ResponseBody
    String getCompany(@RequestHeader MultiValueMap<String,String> headers, @SpringQueryMap OaCompanyRequestDTO dto);

    /**
     * 获取现有公司树
     * @param headers
     * @return
     */
    @GetMapping(value = "/api/public/browser/data/4")
    @ResponseBody
    String getCompanyTree(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取公司下人员的sessionKey
     * @param headers
     * @param id
     * @return
     */
    @PostMapping(value = "/api/hrm/organization/getResourceSearchList",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getCompanyEmpSessionKey(@RequestHeader MultiValueMap<String,String> headers,@RequestBody Integer id);

    /**
     * 通过sessionKey获取数据
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/hrm/organization/getResourceSearchList",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getDataBySessionKey(@RequestHeader MultiValueMap<String,String> headers, @RequestBody OaDataKeyDTO dto);
}
