package com.steccn.portal.util;

import cn.hutool.core.map.MapUtil;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.steccn.portal.constant.HikVisionConstant;

import java.util.Map;

/**
 * 海康威视请求工具类
 *
 * @author AndrewLuo
 * @date 2022/11/23
 */
public class HikVisionUtil {
//     STEP1：设置平台参数，根据实际情况,设置host appkey appsecret 三个参数.
    static {
        // iSC平台实际的ip端口
        ArtemisConfig.host = HikVisionConstant.HIK_HOST;
        // 秘钥appkey
        ArtemisConfig.appKey = HikVisionConstant.HIK_APP_KEY;
        // 秘钥appSecret
        ArtemisConfig.appSecret = HikVisionConstant.HIK_APP_SECRET;
    }

    // STEP2：设置OpenAPI接口的上下文
    private static final String ARTEMIS_PATH = HikVisionConstant.PATH;

    public static String callGetApi(String api,Map<String,String> queries){
        //STEP3:设置接口的URI地址
        final String getCamsApi = ARTEMIS_PATH + api;
        Map<String, String> path = MapUtil.newHashMap();
        path.put("https://",getCamsApi);

        //STEP5:调用请求接口
        String result = ArtemisHttpUtil.doGetArtemis(path,queries,null,"application/json");

        return result;
    }
}
