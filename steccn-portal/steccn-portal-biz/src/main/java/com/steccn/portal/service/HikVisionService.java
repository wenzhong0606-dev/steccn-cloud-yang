package com.steccn.portal.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.steccn.common.vo.R;
import com.steccn.portal.constant.HikVisionConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 海康威视service
 *
 * @author AndrewLuo
 * @date 2022/11/22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HikVisionService {

    /**
     * 获取登录token
     * @param userCode
     * @return
     */
    public String getToken(String userCode){
        //组装接口url地址
        Map<String, String> path = MapUtil.newHashMap();
        path.put("https://", HikVisionConstant.PATH + "/api/cas/v1/tgt/login");

        Map<String,String> querys = MapUtil.newHashMap();
        querys.put("language","zh_CN");
        querys.put("service",HikVisionConstant.HIK_HOST);
        querys.put("userCode",userCode);

        String result = ArtemisHttpUtil.doGetArtemis(path,querys,null,"application/json");
        JSONObject resultObj = JSONObject.parseObject(result);
        String data = resultObj.getString("data");
        String token = JSONObject.parseObject(data).getString("token");


        return token;
    }
}
