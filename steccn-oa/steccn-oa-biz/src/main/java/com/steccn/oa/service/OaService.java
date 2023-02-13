package com.steccn.oa.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.*;
import com.steccn.oa.vo.OaCompanyVO;
import com.steccn.oa.vo.OaEmpVO;
import com.steccn.oa.vo.OaToDoWorkflowVO;
import com.steccn.oa.vo.OaWorkflowCountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import weaver.rsa.security.RSA;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        log.debug(result);
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
    @Cacheable(cacheNames = "OaToken#1500",unless = "#result.status == false")
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
    public JSONArray getMenuList(String userId){
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        //先获取apiToken
        OaApiTokenResult oaApiTokenResult = this.getApiToken();;
        if(!oaApiTokenResult.isStatus()){
            OaMenuResult oaMenuResult = new OaMenuResult();
            oaMenuResult.setStatus(false);
            oaMenuResults.add(oaMenuResult);
            log.error(oaApiTokenResult.getMsg());
            return new JSONArray();
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
            JSONArray jsonArray = JSONObject.parseObject(result).getJSONArray(OaConstant.OA_JSON_DATA);
            //递归拿到树状数据
//            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATA)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).isEmpty()){
//                for(int i = 0; i < jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).size(); i++){
//                    oaMenuResults.add(this.transferJsonToObject(jsonObject.getJSONArray(OaConstant.OA_JSON_DATA).getJSONObject(i)));
//                }
//            }
            return jsonArray;
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
            MultiValueMap<String, String> headers = this.setHeaders();
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

    /**
     * 通过用户名获取oa用户信息
     * @return  OaUserInfo
     */
    public List<OaUserIdAndName> getAll(){
        List<OaUserIdAndName> oaUserIdAndName = new ArrayList<>();
        //放入Token
        OaApiTokenResult oaApiTokenResult = this.getApiToken();;
        if(!oaApiTokenResult.isStatus()){
            log.error(oaApiTokenResult.getMsg());
            return oaUserIdAndName;
        }else{
            MultiValueMap<String, String> headers = setHeaders();
            String result = oaApi.getAllOaUser(headers);
            //转成对象
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).isEmpty()){
                JSONArray jsonArray = jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS);
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject objTmp = jsonArray.getJSONObject(i);
                    OaUserIdAndName info = new OaUserIdAndName();
                    info.setId(objTmp.getInteger("id"));
                    info.setName(objTmp.getString("name"));
                    oaUserIdAndName.add(info);
                }
            }
            return oaUserIdAndName;
        }
    }

    /**
     * OA获取公司列表
     * @param id
     * @return
     */
    public List<OaCompanyVO> getCompany(Integer id){
        MultiValueMap<String, String> headers = this.setHeaders();
        OaCompanyRequestDTO dto = new OaCompanyRequestDTO();
        List<OaCompanyVO> vos = new ArrayList<>();
        String result;
        //若id不为-1则正常调用，否则返回二级数据
        if(!id.equals(OaConstant.NONE)){
            //oa数据
            dto.setShowCanceled("");
            dto.setRightStr(OaConstant.OA_RIGHT_STRING);
            dto.setType(OaConstant.YES);
            dto.setId(id);
            dto.setIsVirtual(OaConstant.NO);
            dto.setKeyword("");
            dto.setVirtualCompanyid(OaConstant.YES);
            dto.setIsLoadSubDepartment(OaConstant.YES);
            result = oaApi.getCompany(headers,dto);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS);
            vos = jsonArray.toJavaList(OaCompanyVO.class);
        }else {
            result = oaApi.getCompanyTree(headers);
            JSONObject jsonObject = JSONObject.parseObject(result);
            OaCompanyVO vo = jsonObject.getJSONObject(OaConstant.OA_JSON_DATAS).toJavaObject(OaCompanyVO.class);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 根据分部id和名字查询人物
     * @param dto
     * @return
     */
    public List<OaEmpVO> getEmpByCondition(OaEmpDTO dto){
        //若传父亲节点，则默认父亲节点下的子节点
        if(dto.getId()!=null && dto.getId().equals(OaConstant.NO)){
            dto.setId(OaConstant.YES);
        }
        MultiValueMap<String, String> headers = setHeaders();
        String sessionResult = oaApi.getCompanyEmpSessionKey(headers,dto);
        //获得sessionKey
        String sessionKey = JSONObject.parseObject(sessionResult).getString(OaConstant.OA_SESSION_KEY);
        OaDataKeyDTO dataKey = new OaDataKeyDTO();
        dataKey.setDataKey(sessionKey);
        if(dto.getCurrent()!=null){
            dataKey.setCurrent(dto.getCurrent());
        }
        String result = oaApi.getDataBySessionKey(headers,dataKey);
        JSONArray data =  JSONObject.parseObject(result).getJSONArray(OaConstant.OA_JSON_DATAS);
        List<OaEmpVO> vos = data.toJavaList(OaEmpVO.class);
        return vos;
    }

    /**
     * 获取待办数据
     * @param flag 0待办 1待阅
     * @return
     */
    public List<OaToDoWorkflowVO> getToDoList(Integer flag){
        OaToDoWorkFlowDTO dto = new OaToDoWorkFlowDTO();
        dto.setUserId(SecurityUtils.getUser().getUserId().toString());
        dto.setPageNo(OaConstant.YES);
        //默认8条数据
        dto.setPageSize(5);
        //待办数据需要修改userId
        MultiValueMap<String, String> headers = this.setHeaders();
        RSA rsa = new RSA();
        String userId = rsa.encrypt(null,SecurityUtils.getUser().getUserId().toString(),null, "utf-8",OaConstant.OA_SPK,false);
        headers.remove("userid");
        headers.set("userid",userId);
        String result = "";
        //0为待办列表,1为待阅列表
        if(flag.equals(0)){
            result = oaApi.getToDoWorkflow(headers,dto);
        }else {
            result = oaApi.getToReadWorkflow(headers,dto);
        }
        JSONArray arr = JSONArray.parseArray(result);
        List<OaToDoWorkflowVO> vos = arr.toJavaList(OaToDoWorkflowVO.class);
        //判断是否已读，已读则有operateTime
        if(CollUtil.isNotEmpty(vos)){
            vos.forEach(tmp->{
                //判断是否已读
                if(StrUtil.isNotBlank(tmp.getOperateTime())){
                    tmp.setIsRead(OaConstant.YES);
                }else {
                    tmp.setIsRead(OaConstant.NO);
                }
                //判断时间差
                if(StrUtil.isNotBlank(tmp.getReceiveTime())){
                    LocalDateTime receiveTime = LocalDateTimeUtil.parse(tmp.getReceiveTime(),"yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    long dayBetween = LocalDateTimeUtil.between(receiveTime, now, ChronoUnit.DAYS);
                    long hourBetween = LocalDateTimeUtil.between(receiveTime, now, ChronoUnit.HOURS);
                    long minBetween = LocalDateTimeUtil.between(receiveTime, now, ChronoUnit.MINUTES);
                    long secBetween = LocalDateTimeUtil.between(receiveTime, now, ChronoUnit.SECONDS);
                    if(secBetween <= 60){
                        //不超过一分钟则一分钟
                        tmp.setTimePeriod(OaConstant.ONE);
                        tmp.setTimePeriod(OaConstant.BEFORE_MINUTES);
                    } else if(secBetween >60 && minBetween <= 60){
                        //超过一分钟小于一小时
                        tmp.setTimePeriod(String.valueOf(minBetween));
                        tmp.setTimePeriod(OaConstant.BEFORE_MINUTES);
                    } else if(minBetween > 60 && hourBetween <= 24){
                        //超过一小时小于一天
                        tmp.setTimePeriod(String.valueOf(hourBetween));
                        tmp.setTimePeriod(OaConstant.BEFORE_HOURS);
                    } else {
                        tmp.setTimePeriod("");
                        tmp.setTimePeriodUnit(receiveTime.getMonthValue()+"-"+ receiveTime.getDayOfMonth());
                    }
                }
            });
        }
        return vos;
    }

    /**
     * 新建流程
     */
    public String getWorkFlow(OaWorkFlowParamDTO dto){
        OaWorkflowDTO oaWorkflowDTO = new OaWorkflowDTO();
        BeanUtils.copyProperties(dto,oaWorkflowDTO);
        if(CollUtil.isNotEmpty(dto.getMainData())){
            oaWorkflowDTO.setMainData(JSONObject.toJSONString(dto.getMainData()));
        }
        if(Objects.nonNull(dto.getDetailData())){
            oaWorkflowDTO.setDetailData(JSONObject.toJSONString(dto.getDetailData()));
        }
        if(CollUtil.isNotEmpty(dto.getOtherParams())){
            oaWorkflowDTO.setOtherParams(JSONObject.toJSONString(dto.getOtherParams()));
        }
        String result = oaApi.postProcess(this.setHeaders(),oaWorkflowDTO);
        return result;
    }

    /**
     * 获取流程状态的数量
     * @return
     */
    public OaWorkflowCountVO getWorkflowCount(){
        OaWorkflowCountVO vo = new OaWorkflowCountVO();
        MultiValueMap<String, String> headers = this.setHeaders();
        RSA rsa = new RSA();
        String userId = rsa.encrypt(null,SecurityUtils.getUser().getUserId().toString(),null, "utf-8",OaConstant.OA_SPK,false);
        headers.remove("userid");
        headers.set("userid",userId);
        String toDoWorkflowCount = oaApi.getToDoWorkflowCount(headers);
        String createdWorkflowCount = oaApi.getCreatedWorkflowCount(headers);
        String handledWorkflowCount = oaApi.getHandledWorkflowCount(headers);
        String rejectedWorkflowCount = oaApi.getRejectedWorkflowCount(headers);
        //我的待办
        if(StrUtil.isNumeric(toDoWorkflowCount)){
            vo.setTodoCount(Integer.parseInt(toDoWorkflowCount));
        }else {
            vo.setTodoCount(OaConstant.NO);
        }

        //我提交的
        if(StrUtil.isNumeric(createdWorkflowCount)){
            vo.setSubmitCount(Integer.parseInt(createdWorkflowCount));
        }else {
            vo.setSubmitCount(OaConstant.NO);
        }

        //我已处理的
        if(StrUtil.isNumeric(handledWorkflowCount)){
            vo.setFinishCount(Integer.parseInt(handledWorkflowCount));
        }else {
            vo.setFinishCount(OaConstant.NO);
        }
        //已驳回
        if(StrUtil.isNumeric(rejectedWorkflowCount)){
            vo.setRejectCount(Integer.parseInt(rejectedWorkflowCount));
        }else {
            vo.setRejectCount(OaConstant.NO);
        }
        return vo;
    }

    /**
     * 设置请求头
     * @return
     */
    public MultiValueMap<String, String> setHeaders(){
        //设置请求头
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //把Token放入header中
        headers.set("appid",OaConstant.OA_API_APPID);
        headers.set("token",this.getApiToken().getToken());
        headers.set("userid",OaConstant.OA_SYSTEM_USERID);
        return headers;
    }
}
