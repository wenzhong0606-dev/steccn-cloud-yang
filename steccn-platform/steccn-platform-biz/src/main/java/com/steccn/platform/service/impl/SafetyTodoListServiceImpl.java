package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSafetyFeignApi;
import com.steccn.platform.service.SafetyTodoListService;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.vo.SafetyTodoListRouterObjVO;
import com.steccn.platform.vo.SafetyTodoListVO;
import com.steccn.platform.vo.SafetyTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * 安环待办列表实现类
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Service
@RequiredArgsConstructor
public class SafetyTodoListServiceImpl implements SafetyTodoListService {
    private final RemoteSafetyFeignApi remoteSafetyTokenService;
    private final SafetyTokenService safetyTokenService;

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
            return result;
        }
        return result;
    }
}
