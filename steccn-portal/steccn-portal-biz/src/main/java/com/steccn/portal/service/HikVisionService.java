package com.steccn.portal.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.steccn.portal.constant.HikVisionConstant;
import com.steccn.portal.util.HikVisionUtil;
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
        //组装请求数据
        Map<String,String> queries = MapUtil.newHashMap();
        queries.put("language","zh_CN");
        queries.put("service",HikVisionConstant.SERVICE);
        queries.put("userCode",userCode);

        String api = "/api/cas/v1/tgt/login";
        //装入get请求
        String result = HikVisionUtil.callGetApi(api,queries);
        JSONObject resultObj = JSONObject.parseObject(result);
        String data = resultObj.getString("data");
        String token = JSONObject.parseObject(data).getString("token");


        return token;
    }
}
