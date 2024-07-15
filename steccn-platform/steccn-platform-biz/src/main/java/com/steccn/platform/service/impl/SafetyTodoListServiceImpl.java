package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteSafetyFeignApi;
import com.steccn.platform.service.SafetyTodoListService;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.service.SysMsgService;
import com.steccn.platform.vo.SafetyCockpitVO;
import com.steccn.platform.vo.SafetyTodoListRouterObjVO;
import com.steccn.platform.vo.SafetyTodoListVO;
import com.steccn.platform.vo.SafetyTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 安环待办列表实现类
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyTodoListServiceImpl implements SafetyTodoListService {
    private final RemoteSafetyFeignApi remoteSafetyTokenService;
    private final SafetyTokenService safetyTokenService;
    private final OaInfoService oaInfoService;
    private final SysMsgService sysMsgService;

    @Override
    public List<SafetyTodoListVO> getTodoList() {
        List<SafetyTodoListVO> result = Lists.newArrayList();
        //获取token
        SafetyTokenVO safetyToken = safetyTokenService.getSafetyToken(SecurityUtils.getUser().getUserId());
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization",safetyToken.getToken());
        //拿到值
        String safetyTodoList = remoteSafetyTokenService.getSafetyTodoList(headers,new TupleDTO());
        JSONObject jsonObject = JSONObject.parseObject(safetyTodoList);
        Integer status = jsonObject.getInteger("status");
        if(status.equals(OaConstant.NO)){
            //获取json下的对象
            result = jsonObject.getJSONObject("data").getJSONArray("data_0").toJavaList(SafetyTodoListVO.class);
            if(CollUtil.isNotEmpty(result)){
                result.forEach(tmp -> {
                    SafetyTodoListRouterObjVO routerObj = new SafetyTodoListRouterObjVO();
                    routerObj.setTitle(tmp.getName());
                    routerObj.setPage(tmp.getPage());
                    routerObj.setRemind_code(tmp.getRemindCode());
                    tmp.setRouterObj(routerObj);
                    tmp.setUserInfo(safetyToken);
                });
            }
        }else {
            log.error("接口请求错误",safetyTodoList);
            return result;
        }
        return result;
    }

    @Override
    public Boolean sendMsg() {
        //获取token
        SafetyTokenVO safetyToken = safetyTokenService.getSafetyToken(942L);
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization",safetyToken.getToken());
        //拿到值
        String safetyTodoList = remoteSafetyTokenService.getAllMsg(headers);
        JSONObject jsonObject = JSONObject.parseObject(safetyTodoList);
        Integer status = jsonObject.getInteger("status");
        if(status.equals(OaConstant.NO)){
            //获取json下的对象
            Set<String> userNames = Sets.newHashSet();
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("data_0");
            for(int i = 0;i < jsonArray.size(); i++){
                userNames.add(jsonArray.getJSONObject(i).getString("userName"));
            }
            if(CollUtil.isNotEmpty(userNames)){
                //发送消息
                //根据用户名获取userId
                List<Integer> userIds = FeignOptional.of(() -> oaInfoService.getIdByWorkCode(userNames.stream().collect(Collectors.toList())), FeignConstant.OA_SERVICE_NAME).get();
                SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.SAFETY_NOTICE.getCode());
                sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.SAFETY_NOTICE.getTitle());
                sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.SAFETY_NOTICE.getWarnFlag());
                sysMsgDTO.setUserList(userIds.stream().map(Integer::longValue).collect(Collectors.toSet()));
                sysMsgDTO.setPushType(OaConstant.NUM_1);
                sysMsgDTO.setContent(SysMsgConstant.MsgTypeEnum.SAFETY_NOTICE.getTemplate());
                sysMsgService.sendMsg(sysMsgDTO);
            }
        }else {
            log.error("接口请求错误",safetyTodoList);
        }
        return true;
    }

    @Override
    public SafetyCockpitVO cockpitDashboard() {
        //获取token
        SafetyTokenVO safetyToken = safetyTokenService.getSafetyToken(942L);
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization",safetyToken.getToken());
        //拿到值
        String safetyTodoList = remoteSafetyTokenService.cockpitDashboard(headers,new TupleDTO());
        JSONObject jsonObject = JSONObject.parseObject(safetyTodoList);
        Integer status = jsonObject.getInteger("status");
        if(status.equals(OaConstant.NO)){
            SafetyCockpitVO vo = jsonObject.getJSONObject("data").getJSONObject("data_0").toJavaObject(SafetyCockpitVO.class);
            return vo;
        }else {
            log.error("接口请求错误",safetyTodoList);
        }
        return null;
    }
}
