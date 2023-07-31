package com.steccn.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SafetyUserNameDTO;
import com.steccn.platform.feign.RemoteSafetyFeignApi;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.vo.SafetyTokenVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 获取安环token实现类
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Service
@RequiredArgsConstructor
public class SafetyTokenServiceImpl implements SafetyTokenService {
    private final RemoteSafetyFeignApi remoteSafetyTokenService;

    @Override
    public SafetyTokenVO getSafetyToken() {
        SafetyTokenVO result = new SafetyTokenVO();
        SafetyUserNameDTO dto = new SafetyUserNameDTO();
        //获取登录用户的用户名
        String userName = SecurityUtils.getUser().getUsername();
        dto.setNicaicai(userName+"1234");
        //请求首自信
        String json = remoteSafetyTokenService.getSafetyToken(dto);
        JSONObject jsonObject = JSONObject.parseObject(json);
        Integer status = jsonObject.getInteger("status");
        if(status.equals(OaConstant.NO)){
            //获取token
            String token = jsonObject.getJSONObject("data").getJSONArray("data_0").getJSONObject(0).getString("token");
            result.setToken(token);
            result.setUserInfo(jsonObject.getJSONObject("data").getJSONArray("data_0").getJSONObject(0).toJSONString().replace("\"", ""));
        }else {
            throw new BusinessException("安环系统中没有该用户,请联系管理员");
        }
        return result;
    }
}
