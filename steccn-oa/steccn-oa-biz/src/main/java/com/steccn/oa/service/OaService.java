package com.steccn.oa.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.query.MPJQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.OutSystemOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.QtChartPieVO;
import com.steccn.common.vo.QtChartPieValueVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.domain.Docdetaillog;
import com.steccn.oa.domain.Hrmdepartment;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.*;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.mapper.OaWorkflowDeleteDetailMapper;
import com.steccn.oa.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import weaver.rsa.security.RSA;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    private final OaTokenService oaTokenService;
	private final HrmresourceService hrmresourceService;
    private final OaWorkflowDeleteDetailMapper oaWorkflowDeleteDetailMapper;
    private final DocdetaillogService docdetaillogService;
    private final KqGroupmemberMapper kqGroupmemberMapper;
    private final HrmdepartmentService hrmdepartmentService;
    private final StringRedisTemplate stringRedisTemplate;
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
        String result = OutSystemOptional.of(() -> oaApi.checkLogin(loginParam), FeignConstant.OA_SERVICE_NAME).get();
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
        String result = OutSystemOptional.of(() -> oaApi.getSsoToken(oaSSoTokenParam), FeignConstant.OA_SERVICE_NAME).get();
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
     * 获取oa菜单请求
     * @param userId
     * @return List<OaMenuResult>
     */
    public JSONArray getMenuList(String userId){
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        //先获取apiToken
        OaApiTokenResult oaApiTokenResult = oaTokenService.getApiToken();
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

            String result = OutSystemOptional.of(() -> oaApi.getOaMenu(headers), FeignConstant.OA_SERVICE_NAME).get();
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
     * 获取oa菜单请求
     * @param userId
     * @return List<OaMenuResult>
     */
    public JSONArray getMenuList2(String userId){
        List<OaMenuResult> oaMenuResults = new ArrayList<>();
        //先获取apiToken
        OaApiTokenResult oaApiTokenResult = oaTokenService.getApiToken();
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

            String result = OutSystemOptional.of(() -> oaApi.getOaAccountMenu(headers), FeignConstant.OA_SERVICE_NAME).get();
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
        //先查用户表
        Hrmresource hrmresource = hrmresourceService.getOne(new LambdaQueryWrapper<Hrmresource>()
                .select(Hrmresource::getId,Hrmresource::getLastname,Hrmresource::getMessagerurl,Hrmresource::getDepartmentid)
                .eq(Hrmresource::getLoginid, loginId)
                .ne(Hrmresource::getStatus, OaConstant.NUM_5),false);
        if(Objects.isNull(hrmresource)){
            oaUserInfo.setStatus(false);
            log.error("没有获取userInfo");
            return oaUserInfo;
        }
        oaUserInfo.setStatus(true);
        oaUserInfo.setName(hrmresource.getLastname());
        oaUserInfo.setHeadPortrait(OaConstant.OA_WEAVER_SITE+hrmresource.getMessagerurl());
        oaUserInfo.setOaUserId(hrmresource.getId());
        Hrmdepartment hrmdepartment = hrmdepartmentService.getOne(new LambdaQueryWrapper<Hrmdepartment>()
                .select(Hrmdepartment::getDepartmentname)
                .eq(Hrmdepartment::getId, hrmresource.getDepartmentid()), false);
        oaUserInfo.setDepartmentName(hrmdepartment.getDepartmentname());
        return oaUserInfo;
        //放入Token
//        OaApiTokenResult oaApiTokenResult = oaTokenService.getApiToken();
//        if(!oaApiTokenResult.isStatus()){
//            oaUserInfo.setStatus(false);
//            log.error(oaApiTokenResult.getMsg());
//            return oaUserInfo;
//        }else{
//            MultiValueMap<String, String> headers = this.setHeaders();
//            OaUserParam oaUserParam = new OaUserParam();
//            OaUserIdAndName oaUserIdAndName = hrmresourceService.getNameByLoginId(loginId);
//            if(Objects.isNull(oaUserIdAndName)){
//                oaUserInfo.setStatus(false);
//                log.error("没有获取userInfo");
//                return oaUserInfo;
//            }
//            //根据名称查
//            oaUserParam.setQ(oaUserIdAndName.getName());
//            String result = oaApi.getOaUserInfo(headers,oaUserParam);
//            //转成对象
//            JSONObject jsonObject = JSONObject.parseObject(result);
//            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).isEmpty()){
//                JSONArray data =  JSONObject.parseObject(result).getJSONArray(OaConstant.OA_JSON_DATAS);
//                List<OaUserInfoTmpDTO> infoList = data.toJavaList(OaUserInfoTmpDTO.class);
//                //根据id过滤
//                OaUserInfoTmpDTO one = infoList.stream().filter(v -> v.getId().equals(oaUserIdAndName.getId())).findFirst().orElse(null);
//                oaUserInfo.setDepartmentName(one.getDepartmentname());
//                oaUserInfo.setName(one.getLastname());
//                oaUserInfo.setHeadPortrait(OaConstant.OA_WEAVER_SITE+one.getIcon());
//                oaUserInfo.setStatus(true);
//            }else {
//                oaUserInfo.setStatus(false);
//                log.error("没有获取userInfo");
//            }
//            return oaUserInfo;
//        }
    }

    /**
     * 通过用户名获取oa用户信息
     * @return  OaUserInfo
     */
    public List<OaUserIdAndName> getAll(){
        List<OaUserIdAndName> oaUserIdAndName = new ArrayList<>();
        List<Hrmresource> list = hrmresourceService.list(new LambdaQueryWrapper<Hrmresource>()
                .select(Hrmresource::getId,Hrmresource::getLastname)
                .ne(Hrmresource::getStatus, OaConstant.NUM_5)
                .last("LIMIT 30"));
        list.forEach(tmp->{
            OaUserIdAndName info = new OaUserIdAndName();
            info.setId(tmp.getId());
            info.setName(tmp.getLastname());
            oaUserIdAndName.add(info);
        });
        return oaUserIdAndName;
        //放入Token
//        OaApiTokenResult oaApiTokenResult = oaTokenService.getApiToken();
//        if(!oaApiTokenResult.isStatus()){
//            log.error(oaApiTokenResult.getMsg());
//            return oaUserIdAndName;
//        }else{
//            MultiValueMap<String, String> headers = setHeaders();
//            String result = oaApi.getAllOaUser(headers);
//            //转成对象
//            JSONObject jsonObject = JSONObject.parseObject(result);
//            if(jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS)!=null && !jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS).isEmpty()){
//                JSONArray jsonArray = jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS);
//                for(int i = 0; i < jsonArray.size(); i++){
//                    JSONObject objTmp = jsonArray.getJSONObject(i);
//                    OaUserIdAndName info = new OaUserIdAndName();
//                    info.setId(objTmp.getInteger("id"));
//                    info.setName(objTmp.getString("name"));
//                    oaUserIdAndName.add(info);
//                }
//            }
//            return oaUserIdAndName;
//        }
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
            result = OutSystemOptional.of(() -> oaApi.getCompany(headers, dto), FeignConstant.OA_SERVICE_NAME).get();
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(OaConstant.OA_JSON_DATAS);
            vos = jsonArray.toJavaList(OaCompanyVO.class);
        }else {
            result = OutSystemOptional.of(() -> oaApi.getCompanyTree(headers), FeignConstant.OA_SERVICE_NAME).get();
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
        String sessionResult = OutSystemOptional.of(() -> oaApi.getCompanyEmpSessionKey(headers, dto), FeignConstant.OA_SERVICE_NAME).get();
        //获得sessionKey
        String sessionKey = JSONObject.parseObject(sessionResult).getString(OaConstant.OA_SESSION_KEY);
        OaDataKeyDTO dataKey = new OaDataKeyDTO();
        dataKey.setDataKey(sessionKey);
        if(dto.getCurrent()!=null){
            dataKey.setCurrent(dto.getCurrent());
        }
        String result = OutSystemOptional.of(() -> oaApi.getDataBySessionKey(headers,dataKey), FeignConstant.OA_SERVICE_NAME).get();
        JSONArray data =  JSONObject.parseObject(result).getJSONArray(OaConstant.OA_JSON_DATAS);
        List<OaEmpVO> vos = data.toJavaList(OaEmpVO.class);
        return vos;
    }

    /**
     * 获取oa人员信息
     * @param dto
     * @return
     */
    public List<OaEmpVO> getEmpInfoByCondition(OaEmpStatusDTO dto){
        MultiValueMap<String, String> headers = setHeaders();

        String sessionResult = OutSystemOptional.of(() -> oaApi.getEmpInfoSessionKey(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
        //获得sessionKey
        String sessionKey = JSONObject.parseObject(sessionResult).getString(OaConstant.OA_SESSION_KEY);
        OaDataKeyDTO dataKey = new OaDataKeyDTO();
        if(dto.getCurrent()!=null){
            dataKey.setCurrent(dto.getCurrent());
        }
        if(dto.getPageSize()!=null){
            dataKey.setPageSize(dto.getPageSize());
        }
        dataKey.setDataKey(sessionKey);
        String result = OutSystemOptional.of(() -> oaApi.getDataBySessionKey(headers,dataKey), FeignConstant.OA_SERVICE_NAME).get();
        JSONArray data =  JSONObject.parseObject(result).getJSONArray(OaConstant.OA_JSON_DATAS);
        List<OaEmpVO> vos = data.toJavaList(OaEmpVO.class);
        return vos;
    }

    /**
     * 获取待办数据
     * @param flag 0待办 1待阅 3我提交的
     * @return
     */
    public List<OaToDoWorkflowVO> getToDoList(Integer flag){
        OaToDoWorkFlowDTO dto = new OaToDoWorkFlowDTO();
        dto.setUserId(SecurityUtils.getUser().getUserId().toString());
        dto.setPageNo(OaConstant.YES);
        //默认5条数据
        dto.setPageSize(5);
        //待办数据需要修改userId
        MultiValueMap<String, String> headers = this.setUserIdHeaders();
        String result = "";
        //0为待办列表,1为待阅列表,3已创建列表，4已处理的,5已驳回的
        switch (flag) {
            case 0:
                result = OutSystemOptional.of(() -> oaApi.getToDoWorkflow(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
                break;
            case 1:
                result = OutSystemOptional.of(() -> oaApi.getToReadWorkflow(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
                break;
            case 3:
                result = OutSystemOptional.of(() -> oaApi.getCreatedWorkflow(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
                break;
            case 4:
                result = OutSystemOptional.of(() -> oaApi.getDoneWorkflow(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
                break;
            case 5:
                result = OutSystemOptional.of(() -> oaApi.getRejectedWorkflow(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
                break;
        }
        JSONArray arr = JSONArray.parseArray(result);
        List<OaToDoWorkflowVO> vos = arr.toJavaList(OaToDoWorkflowVO.class);
        //判断是否已读，已读则有operateTime,并判断时间差
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
                        tmp.setTimePeriod(OaConstant.ONE + OaConstant.BEFORE_MINUTES);
                    } else if(secBetween >60 && minBetween <= 60){
                        //超过一分钟小于一小时
                        tmp.setTimePeriod(minBetween + OaConstant.BEFORE_MINUTES);
                    } else if(minBetween > 60 && hourBetween <= 24){
                        //超过一小时小于一天
                        tmp.setTimePeriod(hourBetween + OaConstant.BEFORE_HOURS);
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
    public OaWorkflowStatusVO createWorkFlow(OaWorkFlowParamDTO dto){
        OaWorkflowDTO oaWorkflowDTO = new OaWorkflowDTO();
        BeanUtils.copyProperties(dto,oaWorkflowDTO);
        if(CollUtil.isNotEmpty(dto.getMainData())){
            oaWorkflowDTO.setMainData(JSONObject.toJSONString(dto.getMainData()));
        }
        if(Objects.nonNull(dto.getDetailData())){
            //替换反斜杠
            oaWorkflowDTO.setDetailData(JSONObject.toJSONString(dto.getDetailData()));
        }
        if(CollUtil.isNotEmpty(dto.getOtherParams())){
            oaWorkflowDTO.setOtherParams(JSONObject.toJSONString(dto.getOtherParams()));
        }
        String result = "";
        if(dto.getUserId()!=null){
            result = OutSystemOptional.of(() -> oaApi.postProcess(this.setUserIdHeadersByFeign(dto.getUserId()),oaWorkflowDTO) , FeignConstant.OA_SERVICE_NAME).get();
        }else {
            result = OutSystemOptional.of(() -> oaApi.postProcess(this.setUserIdHeaders(),oaWorkflowDTO) , FeignConstant.OA_SERVICE_NAME).get();
        }
        OaWorkflowStatusVO vo = new OaWorkflowStatusVO();
        JSONObject jsonObject = JSONObject.parseObject(result);
        vo.setCode(jsonObject.getString(OaConstant.OA_JSON_CODE));
        //如果是成功则收到成功消息，否则返回错误信息
        if(vo.getCode().equals(OaConstant.OA_WORKFLOW_SUCCESS)){
            vo.setRequestId(jsonObject.getJSONObject(OaConstant.OA_JSON_DATA).getInteger(OaConstant.OA_WORKFLOW_REQUESTID));
        }else {
            log.info(result);
            if(vo.getCode().equals(OaConstant.OA_WORKFLOW_PARAM_ERROR)){
                vo.setErrMsg(jsonObject.getJSONObject(OaConstant.OA_JSON_ERRMSG).toString());
            }
            vo.setErrMsg(jsonObject.getJSONObject(OaConstant.OA_JSON_ERRMSG).getString(OaConstant.OA_JSON_MSG));
        }
        return vo;
    }

    /**
     * 提交流程
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OaWorkflowStatusVO submitWorkFlow(OaWorkFlowSubmitParamDTO dto){
        OaWorkflowSubmitDTO oaWorkflowSubmitDTO = new OaWorkflowSubmitDTO();
        BeanUtils.copyProperties(dto,oaWorkflowSubmitDTO);
        if(CollUtil.isNotEmpty(dto.getMainData())){
            oaWorkflowSubmitDTO.setMainData(JSONObject.toJSONString(dto.getMainData()));
        }
        if(Objects.nonNull(dto.getDetailData())){
            //替换反斜杠
            oaWorkflowSubmitDTO.setDetailData(JSONObject.toJSONString(dto.getDetailData()).replaceAll("\\\\",""));
        }
        if(CollUtil.isNotEmpty(dto.getOtherParams())){
            oaWorkflowSubmitDTO.setOtherParams(JSONObject.toJSONString(dto.getOtherParams()));
        }
        //如果是不合格品处理，删除不合格品详情表
        if(dto.getWorkFlowId().equals(OaManagerConstant.OaWorkFlow.DEFECT_PRODUCT.getId())){
            oaWorkflowDeleteDetailMapper.deleteUnqualified(dto.getRequestId());
        }
        //如果是客诉，删除客诉详情表
        if(dto.getWorkFlowId().equals(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getId())){
            oaWorkflowDeleteDetailMapper.deleteComplain1(dto.getRequestId());
            oaWorkflowDeleteDetailMapper.deleteComplain2(dto.getRequestId());
        }
        String result = OutSystemOptional.of(() -> oaApi.submitProcess(this.setUserIdHeadersByFeign(dto.getUserId()),oaWorkflowSubmitDTO) , FeignConstant.OA_SERVICE_NAME).get();
        OaWorkflowStatusVO vo = new OaWorkflowStatusVO();
        JSONObject jsonObject = JSONObject.parseObject(result);
        vo.setCode(jsonObject.getString(OaConstant.OA_JSON_CODE));
        if(!vo.getCode().equals(OaConstant.OA_WORKFLOW_SUCCESS)){
            throw new BusinessException("提交失败,删除数据回滚");
        }
        //如果是成功则收到成功消息，否则返回错误信息
//        if(vo.getCode().equals(OaConstant.OA_WORKFLOW_SUCCESS)){
//            vo.setRequestId(jsonObject.getJSONObject(OaConstant.OA_JSON_DATA).getInteger(OaConstant.OA_WORKFLOW_REQUESTID));
//        }else {
//            if(vo.getCode().equals(OaConstant.OA_WORKFLOW_PARAM_ERROR)){
//                vo.setErrMsg(jsonObject.getJSONObject(OaConstant.OA_JSON_ERRMSG).toString());
//            }
//            vo.setErrMsg(jsonObject.getJSONObject(OaConstant.OA_JSON_ERRMSG).getString(OaConstant.OA_JSON_MSG));
//        }
        return vo;
    }

    /**
     * 提交流程
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public OaWorkflowStatusVO doForceDrawBack(OaWorkFlowSubmitParamDTO dto){
        Assert.notNull(dto.getRequestId(),"requestId不能为空");
        Assert.notNull(dto.getRequestId(),"用户Id不能为空");
        OaWorkflowSubmitDTO oaWorkflowSubmitDTO = new OaWorkflowSubmitDTO();
        oaWorkflowSubmitDTO.setRequestId(dto.getRequestId());
        oaWorkflowSubmitDTO.setOtherParams(JSONObject.toJSONString(new OaWorkflowOtherParamsDTO("1")));
        String backResult = OutSystemOptional.of(() -> oaApi.doForceDrawBack(this.setUserIdHeadersByFeign(dto.getUserId()),oaWorkflowSubmitDTO) , FeignConstant.OA_SERVICE_NAME).get();
        String deleteResult = OutSystemOptional.of(() -> oaApi.deleteRequest(this.setUserIdHeadersByFeign(dto.getUserId()),oaWorkflowSubmitDTO) , FeignConstant.OA_SERVICE_NAME).get();
        OaWorkflowStatusVO vo = new OaWorkflowStatusVO();
        JSONObject jsonObject = JSONObject.parseObject(deleteResult);
        vo.setCode(jsonObject.getString(OaConstant.OA_JSON_CODE));
        if(!vo.getCode().equals(OaConstant.OA_WORKFLOW_SUCCESS)){
            throw new BusinessException(vo.getCode());
        }
        return new OaWorkflowStatusVO();
    }



    /**
     * 获取流程状态的数量
     * @return
     */
    public OaWorkflowCountVO getWorkflowCount(){
        OaWorkflowCountVO vo = new OaWorkflowCountVO();
        MultiValueMap<String, String> headers = this.setUserIdHeaders();
        String toDoWorkflowCount  = OutSystemOptional.of(() -> oaApi.getToDoWorkflowCount(headers), FeignConstant.OA_SERVICE_NAME).get();
        String createdWorkflowCount = OutSystemOptional.of(() -> oaApi.getCreatedWorkflowCount(headers), FeignConstant.OA_SERVICE_NAME).get();
        String handledWorkflowCount = OutSystemOptional.of(() -> oaApi.getHandledWorkflowCount(headers), FeignConstant.OA_SERVICE_NAME).get();
        String rejectedWorkflowCount = OutSystemOptional.of(() -> oaApi.getRejectedWorkflowCount(headers) , FeignConstant.OA_SERVICE_NAME).get();
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
     * 排版记录
     * @param dto
     * @return
     */
    public List<OaRosterRecordVO> getRosterRecord(OaRosterRecordDTO dto){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization",OaConstant.OA_BASIC_AUTH);
        String rosterRecord = OutSystemOptional.of(() -> oaApi.getRosterRecord(headers, dto), FeignConstant.OA_SERVICE_NAME).get();
        List<OaRosterRecordVO> vos = Lists.newArrayList();
        if(StrUtil.isBlank(rosterRecord)){
            return vos;
        }
        JSONObject jsonObject = JSONObject.parseObject(rosterRecord);
        if(Objects.isNull(jsonObject)){
            return vos;
        }
        JSONArray jsonArray = jsonObject.getJSONArray(OaConstant.OA_CAPITAL_DATA);
        if(Objects.isNull(jsonArray)){
            return vos;
        }
        vos = jsonArray.toJavaList(OaRosterRecordVO.class);
        return vos;
    }

    /**
     * 获取班次班组
     * @param dateDTO filed1为开始时间 filed2为结束时间
     * @return
     */
    public Map<String,OaGroupCountDTO> getClassGroupMap(TupleDTO<LocalDate,LocalDate> dateDTO){
//        调用OA接口返回在本行班次日期&班次”质检“岗位上的所有人员
//        每个人员属于1个或多于1个班组，取人数最多的班组（班组简写为甲班、乙班、丙班）。例如4个人属于甲班，1个人属于乙班，则取甲班
//        取不到人数最多的班组的情况，均取空值
        Map<String,OaGroupCountDTO> resultMap = MapUtil.newHashMap();
        OaRosterRecordDTO dto = new OaRosterRecordDTO();
        if(dateDTO.getField1()==null||dateDTO.getField2()==null){
            throw new BusinessException("开始时间,结束时间不能为空");
        }
        List<OaRosterRecordVO> rosterRecord = Lists.newArrayList();
        dto.setStartdate(dateDTO.getField1().toString());
        dto.setEnddate(dateDTO.getField2().toString());
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN1);
        List<OaRosterRecordVO> rosterRecord1 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord1);
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN2);
        List<OaRosterRecordVO> rosterRecord2 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord2);
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN3);
        List<OaRosterRecordVO> rosterRecord3 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord3);
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN4);
        List<OaRosterRecordVO> rosterRecord4 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord4);
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN5);
        List<OaRosterRecordVO> rosterRecord5 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord5);
        dto.setJobtitle(OaConstant.JOB_TITLE_ZHIJIAN6);
        List<OaRosterRecordVO> rosterRecord6 = getRosterRecord(dto);
        rosterRecord.addAll(rosterRecord6);
        if(CollUtil.isNotEmpty(rosterRecord)){
            //拿到所有workCode对应的userCode
            List<String> workCodes = rosterRecord.stream()
                    .map(OaRosterRecordVO::getWorkcode)
                    .distinct()
                    .collect(Collectors.toList());
            //拿到对应的userid
            List<Hrmresource> hrmresourceList = hrmresourceService.list(new LambdaQueryWrapper<Hrmresource>()
                    .select(Hrmresource::getId,Hrmresource::getWorkcode)
                    .in(Hrmresource::getWorkcode, workCodes));
            Map<String, Integer> workCodeIdMap = hrmresourceList.stream().collect(Collectors.toMap(
                    Hrmresource::getWorkcode,
                    Hrmresource::getId,
                    ((existingValue, newValue) -> existingValue)));
            //把白班和夜班改成早班晚班
            rosterRecord.stream().forEach(tmp->{
                tmp.setUserId(workCodeIdMap.get(tmp.getWorkcode()));
                if(tmp.getSerial().equals("白班")){
                    tmp.setSerial("早班");
                }
                if(tmp.getSerial().equals("夜班")){
                    tmp.setSerial("晚班");
                }
            });
            //根据日期和班次确定list
            Map<String, List<Integer>> dateUserIdMap = rosterRecord.stream()
                    .collect(Collectors.groupingBy(record -> record.getKqdate() + ":" + record.getSerial(),
                            Collectors.mapping(OaRosterRecordVO::getUserId, Collectors.toList())));
            //查询数据库找到人最多的班组
            for(Map.Entry<String,List<Integer>> entry:dateUserIdMap.entrySet()){
                if(CollUtil.isNotEmpty(entry.getValue())){
                    OaGroupCountDTO groupCountDTO = null;
                    //缓存数据,避免多次请求接口缓慢
                    String cachedData = stringRedisTemplate.opsForValue().get("mostClassGroup::"+entry.getKey());
                    if(StrUtil.isBlank(cachedData)){
                        MPJQueryWrapper mpjQueryWrapper = new MPJQueryWrapper<OaGroupCountDTO>()
                                .select("groupid AS groupId,groupname As groupName,Count(typevalue) AS count")
                                .leftJoin("kq_group t1 on t1.id = t.groupid")
                                .isNull("t.isDelete")
                                .in("typevalue",entry.getValue())
                                .groupBy("groupid")
                                .orderByDesc("count")
                                .last("LIMIT 1");
                        groupCountDTO = kqGroupmemberMapper.selectJoinOne(OaGroupCountDTO.class, mpjQueryWrapper);
                        if(Objects.nonNull(groupCountDTO)){
                            stringRedisTemplate.opsForValue().set("mostClassGroup::"+entry.getKey(),JSONObject.toJSONString(groupCountDTO));
                            stringRedisTemplate.expire("mostClassGroup::"+entry.getKey(),1,TimeUnit.DAYS);
                        }
                    }else {
                        groupCountDTO = JSONObject.parseObject(cachedData,OaGroupCountDTO.class);
                    }
                    if(Objects.nonNull(groupCountDTO)){
                        groupCountDTO = classGroupNameMap(groupCountDTO);
                        resultMap.put(entry.getKey(),groupCountDTO);
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * 获取人员的sessionKey
     * @return
     */
    public List<OaEmpVO> getHrmSearchResult(OaHrmDTO dto){
        MultiValueMap<String, String> headers = this.setHeaders();
        String sessionKeyResult = OutSystemOptional.of(() -> oaApi.getHrmSearchResult(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
        String sessionKey = JSONObject.parseObject(sessionKeyResult).getString(OaConstant.OA_SESSION_KEY);
        OaDataKeyDTO dataKey = new OaDataKeyDTO();
        dataKey.setDataKey(sessionKey);
        String dataBySessionKey = OutSystemOptional.of(() -> oaApi.getDataBySessionKey(headers, dataKey), FeignConstant.OA_SERVICE_NAME).get();
        JSONArray data =  JSONObject.parseObject(dataBySessionKey).getJSONArray(OaConstant.OA_JSON_DATAS);
        List<OaEmpVO> vos = data.toJavaList(OaEmpVO.class);
        return vos;
    }

    /**
     * 根据部门信息查询部门详情
     * @param dto
     * @return
     */
    public List<OaOrgDetailVO> getOrgDetailByCondition(OaOrgDetailDTO dto){
        MultiValueMap<String, String> headers = this.setHeaders();
        //获取sessionKey
        String sessionKeyResult = OutSystemOptional.of(() -> oaApi.getOrgDetail(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
        String sessionKey = JSONObject.parseObject(sessionKeyResult).getString(OaConstant.OA_SESSION_KEY);
        //根据sessionKey获取数据
        OaDataKeyDTO dataKey = new OaDataKeyDTO();
        dataKey.setDataKey(sessionKey);
        String dataBySessionKey = OutSystemOptional.of(() -> oaApi.getDataBySessionKey(headers, dataKey), FeignConstant.OA_SERVICE_NAME).get();
        JSONArray data =  JSONObject.parseObject(dataBySessionKey).getJSONArray(OaConstant.OA_JSON_DATAS);
        List<OaOrgDetailVO> vos = data.toJavaList(OaOrgDetailVO.class);
        return vos;
    }

    /**
     * 根据部门信息查询部门详情,restful版本
     * @param dto
     * @return
     */
    public List<OaOrgDetailVO> getOrgDetailByConditionV2(OaOrgDetailParamDTO dto){
        MultiValueMap<String, String> headers = this.setHeaders();
        String result = OutSystemOptional.of(() -> oaApi.getCompanyByOrg(headers,dto), FeignConstant.OA_SERVICE_NAME).get();
        JSONArray data =  JSONObject.parseObject(result).getJSONObject(OaConstant.OA_JSON_DATA).getJSONArray(OaConstant.OA_JSON_DATA_LIST);
        List<OaOrgDetailVO> vos = data.toJavaList(OaOrgDetailVO.class);
        return vos;
    }

    /**
     * Oa流程跳转文档信息
     * @param url
     * @return
     */
    public OaSaveDocVO saveDoc(String url,String docName){
        MultiValueMap<String, String> headers = this.setHeaders();
        OaDocDTO dto = new OaDocDTO();
        //默认目录是93
        dto.setSeccategory(93);
        dto.setDoccontent("<html>\n" +
                "<head>\n" +
                "\t<title></title>\n" +
                "\t<link href=\"/cloudstore/resource/pc/ckeditor-4.6.2/weaver/contents.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "\t<link href=\"/cloudstore/resource/pc/ckeditor-4.6.2/plugins/copyformatting/styles/copyformatting.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "\t<link href=\"/cloudstore/resource/pc/ckeditor-4.6.2/plugins/tableselection/styles/tableselection.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "\t<link href=\"/cloudstore/resource/pc/ckeditor-4.6.2/plugins/image2/styles/image2.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "\t<link href=\"/cloudstore/resource/pc/ckeditor-4.6.2/plugins/widget/styles/widget.css\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "</head>\n" +
                "<body style=\"height: 753px;\">\n" +
                "<p><a href=\"" +
                url +
                "\" target=\"_blank\"><font color=\"#3498db\"><span style=\"font-size:16px;\">点击查看详情</span><span style=\"font-size:9px;\"></span></font></a><span style=\"font-size:20px;\"></span></p>\n" +
                "</body>\n" +
                "</html>");
        dto.setDocSubject(docName);
        String result = OutSystemOptional.of(() -> oaApi.saveDoc(headers, dto), FeignConstant.OA_SERVICE_NAME).get();
        OaSaveDocVO oaSaveDocVO = JSONObject.parseObject(result, OaSaveDocVO.class);
        return oaSaveDocVO;
    }

    /**
     * 获取我的考勤信息
     * @return
     */
    public OaAttendanceVO getMyAttendance(){
        OaAttendanceDTO dto = new OaAttendanceDTO();
        dto.setResourceId(SecurityUtils.getUser().getUserId().toString());
        dto.setType(OaConstant.TWO);
        dto.setTypevalue(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY-MM")));
        String result = OutSystemOptional.of(() -> oaApi.getMyAttendance(setUserIdHeaders(), dto), FeignConstant.OA_SERVICE_NAME).get();
        OaAttendanceVO vo = new OaAttendanceVO();
        JSONObject jsonObject = JSONObject.parseObject(result);
        //出勤相关
        JSONObject groupItem1 = jsonObject.getJSONObject(OaConstant.OA_JSON_GROUPITEM1);
        //应出勤
        vo.setRequiredNum(groupItem1.getBigDecimal(OaConstant.OA_JSON_VALUE));
        JSONArray groupItem1Array = groupItem1.getJSONArray(OaConstant.OA_JSON_ITEMS);
        //未出勤
        vo.setUnAttendedNum(groupItem1Array.getJSONObject(0).getBigDecimal(OaConstant.OA_JSON_VALUE));
        //实际出勤
        vo.setActualNum(groupItem1Array.getJSONObject(1).getBigDecimal(OaConstant.OA_JSON_VALUE));
        //迟到相关
        JSONArray groupItem3Array = jsonObject.getJSONObject(OaConstant.OA_JSON_GROUPITEM3).getJSONArray(OaConstant.OA_JSON_ITEMS);
        vo.setLateNum(groupItem3Array.getJSONObject(0).getBigDecimal(OaConstant.OA_JSON_VALUE).add(groupItem3Array.getJSONObject(1).getBigDecimal(OaConstant.OA_JSON_VALUE)));
        //早退
        vo.setEarlyNum(groupItem3Array.getJSONObject(2).getBigDecimal(OaConstant.OA_JSON_VALUE).add(groupItem3Array.getJSONObject(3).getBigDecimal(OaConstant.OA_JSON_VALUE)));
        //旷工
        vo.setAbsentNum(groupItem3Array.getJSONObject(4).getBigDecimal(OaConstant.OA_JSON_VALUE));
        //漏签
        vo.setMissedNum(groupItem3Array.getJSONObject(5).getBigDecimal(OaConstant.OA_JSON_VALUE));
        //图表
        QtChartPieVO chart = new QtChartPieVO<BigDecimal>();
        List<QtChartPieValueVO<BigDecimal>> data = Lists.newArrayList();
        if (!vo.getRequiredNum().equals(BigDecimal.ZERO)) {
            QtChartPieValueVO<BigDecimal> tmp1 = new QtChartPieValueVO();
            tmp1.setValue(BigDecimalUtil.divide(vo.getActualNum(), vo.getRequiredNum(), 4));
            tmp1.setName("实际出勤占比");
            data.add(tmp1);
            QtChartPieValueVO tmp2 = new QtChartPieValueVO<BigDecimal>();
            tmp2.setName("未出勤占比");
            tmp2.setValue(BigDecimalUtil.divide(vo.getUnAttendedNum(), vo.getRequiredNum(), 4));
            data.add(tmp2);
        }
        chart.setData(data);
        vo.setChart(chart);
        return vo;
    }

    public List<OaNewsVO> getNewsInfo(){
        List<OaNewsVO> oaNewsVOS = Lists.newArrayList();
        MultiValueMap<String, String> headers = this.setHeaders();
        //1、先获取新闻列表的sessionKey
        String newList = OutSystemOptional.of(() -> oaApi.getNewList(headers), FeignConstant.OA_SERVICE_NAME).get();
        String sessionKey = JSONObject.parseObject(newList).getString(OaConstant.OA_SESSION_KEY);
        OaDataKeyDTO dataKeyDTO = new OaDataKeyDTO();
        dataKeyDTO.setDataKey(sessionKey);
        dataKeyDTO.setCurrent(1);
        dataKeyDTO.setPageSize(6);
        String dataBySessionKey = OutSystemOptional.of(() -> oaApi.getDataBySessionKey(headers, dataKeyDTO), FeignConstant.OA_SERVICE_NAME).get();
        List<OaNewsListDTO> oaNewsListDTOS = JSONObject.parseObject(dataBySessionKey).getJSONArray(OaConstant.OA_JSON_DATAS).toJavaList(OaNewsListDTO.class);
        //2、遍历获取每个页面的doc
        if(CollUtil.isNotEmpty(oaNewsListDTOS)){
            oaNewsListDTOS.forEach(tmp -> {
                OaNewsVO oaNewsVO = new OaNewsVO();
                oaNewsVO.setNewsId(tmp.getId());
                oaNewsVO.setNewsName(tmp.getFrontpagename());
                //3、获取docId对应的时间
                String newInfo = OutSystemOptional.of(() -> oaApi.getNewInfo(headers, tmp.getId()), FeignConstant.OA_SERVICE_NAME).get();
                String docIdStr = JSONObject.parseObject(newInfo).getJSONObject(OaConstant.OA_JSON_DATA).getString(OaConstant.OA_JSON_IMPORTDOCID);
                if(StrUtil.isNotBlank(docIdStr)){
                    String[] split = docIdStr.split(",");
                    String docId = split[0];
                    //获取文档最新操作时间
                    Docdetaillog docdetaillog = docdetaillogService.getOne(new LambdaQueryWrapper<Docdetaillog>()
                            .select(Docdetaillog::getOperatedate)
                            .eq(Docdetaillog::getDocid, docId)
                            .orderByDesc(Docdetaillog::getOperatedate)
                            .last("LIMIT 1"));
                    if(Objects.nonNull(docdetaillog)){
                        String[] dateSplit = docdetaillog.getOperatedate().split("-");
                        oaNewsVO.setCreateTime(dateSplit[1]+"-"+dateSplit[2]);
                    }
                }
                oaNewsVOS.add(oaNewsVO);
            });
        }
        return oaNewsVOS;
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
        headers.set("token",oaTokenService.getApiToken().getToken());
        headers.set("userid",OaConstant.OA_SYSTEM_USERID);
        return headers;
    }

    /**
     * 设置当前用户登录的请求头
     * @return
     */
    public MultiValueMap<String, String> setUserIdHeaders(){
        MultiValueMap<String, String> headers = this.setHeaders();
        RSA rsa = new RSA();
        String userId = rsa.encrypt(null,SecurityUtils.getUser().getUserId().toString(),null, "utf-8",OaConstant.OA_SPK,false);
        headers.remove("userid");
        headers.set("userid",userId);
        return headers;
    }

    /**
     * 设置当前用户登录的请求头
     * @return
     */
    public MultiValueMap<String, String> setUserIdHeadersByFeign(Long userId){
        MultiValueMap<String, String> headers = this.setHeaders();
        RSA rsa = new RSA();
        String userIdStr = rsa.encrypt(null,userId.toString(),null, "utf-8",OaConstant.OA_SPK,false);
        headers.remove("userid");
        headers.set("userid",userIdStr);
        return headers;
    }

    /**
     * 凡是包含甲班的数据都显示甲班
     * @param groupCountDTO
     * @return
     */
    private OaGroupCountDTO classGroupNameMap(OaGroupCountDTO groupCountDTO){
        String classGroupName = groupCountDTO.getGroupName();
        if(classGroupName.contains(OaManagerConstant.OaTeamGroupMapping.A.getName())){
            groupCountDTO.setId(groupCountDTO.getGroupId());
            groupCountDTO.setGroupName(OaManagerConstant.OaTeamGroupMapping.A.getName());
            groupCountDTO.setGroupId(OaManagerConstant.OaTeamGroupMapping.A.getCode());
            return groupCountDTO;
        }
        if(classGroupName.contains(OaManagerConstant.OaTeamGroupMapping.B.getName())){
            groupCountDTO.setId(groupCountDTO.getGroupId());
            groupCountDTO.setGroupName(OaManagerConstant.OaTeamGroupMapping.B.getName());
            groupCountDTO.setGroupId(OaManagerConstant.OaTeamGroupMapping.B.getCode());
            return groupCountDTO;
        }
        if(classGroupName.contains(OaManagerConstant.OaTeamGroupMapping.C.getName())){
            groupCountDTO.setId(groupCountDTO.getGroupId());
            groupCountDTO.setGroupName(OaManagerConstant.OaTeamGroupMapping.C.getName());
            groupCountDTO.setGroupId(OaManagerConstant.OaTeamGroupMapping.C.getCode());
            return groupCountDTO;
        }
        return groupCountDTO;
    }

}
