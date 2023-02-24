package com.steccn.oa.service;

import com.steccn.oa.dto.*;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * 调用OA系统API
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@FeignClient(name = "oaApi",url = "${oa.weaver-site}",decode404 = true)
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
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/hrm/organization/getResourceSearchList",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getCompanyEmpSessionKey(@RequestHeader MultiValueMap<String,String> headers,@RequestBody OaEmpDTO dto);

    /**
     * 通过sessionKey获取数据
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/ec/dev/table/datas",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getDataBySessionKey(@RequestHeader MultiValueMap<String,String> headers, @RequestBody OaDataKeyDTO dto);

    /**
     * 获取待办数据
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getToDoWorkflowRequestList",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getToDoWorkflow(@RequestHeader MultiValueMap<String,String> headers, @RequestBody OaToDoWorkFlowDTO dto);

    /**
     * 获取待办数据数量
     * @param headers
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getToDoWorkflowRequestCount",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getToDoWorkflowCount(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取所发起的流程数量(我提交的)
     * @param headers
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getMyWorkflowRequestCount",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getCreatedWorkflowCount(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取已办流程数量(我已处理的)
     * @param headers
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getHandledWorkflowRequestCount4Ofs",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getHandledWorkflowCount(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取已退回的流程数量(已驳回)
     * @param headers
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getBeRejectWorkflowRequestCount",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getRejectedWorkflowCount(@RequestHeader MultiValueMap<String,String> headers);

    /**
     * 获取待阅数据
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/getToBeReadWorkflowRequestList",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    String getToReadWorkflow(@RequestHeader MultiValueMap<String,String> headers, @RequestBody OaToDoWorkFlowDTO dto);

    /**
     * 调用oa流程接口
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/workflow/paService/doCreateRequest",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String postProcess(@RequestHeader MultiValueMap<String,String> headers,@RequestBody OaWorkflowDTO dto);

    /**
     * 调用oa获取人员列表
     * @param headers
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/hrm/search/getHrmSearchResult",headers = {"content-type=application/x-www-form-urlencoded"})
    @Headers(value = {"content-type=application/x-www-form-urlencoded"})
    @ResponseBody
    String getHrmSearchResult(@RequestHeader MultiValueMap<String,String> headers,@RequestBody OaHrmDTO dto);
}
