package com.steccn.hikvision.api;

import com.alibaba.fastjson.JSON;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.steccn.hikvision.entity.Cameras;
import com.steccn.hikvision.entity.PlaybackURLs;
import com.steccn.hikvision.entity.PreviewURLs;


import java.util.HashMap;
import java.util.Map;


/**
 * Auto Create on 2021-10-15 02:26:00
 */
public class VideoApi {
    /**
     * STEP1：设置平台参数，根据实际情况,设置host appkey appsecret 三个参数.
     */
    static {
//        ArtemisConfig.host = "60.171.128.4:1443";
//        ArtemisConfig.appKey = "29714743"; // 秘钥appkey
//        ArtemisConfig.appSecret = "aMYdTfGy7QU0DVFQCQmW";
        ArtemisConfig.host = "192.168.200.100:1443";
        ArtemisConfig.appKey = "24075458"; // 秘钥appkey
        ArtemisConfig.appSecret = "tfgKpdUKLH2IOL9efSzo";
    }

    /**
     * STEP2：设置OpenAPI接口的上下文
     */
    private static final String ARTEMIS_PATH = "/artemis";

    //获取视频设备唯一标识
    public String C(Cameras cameras) {
        String getDataApi = ARTEMIS_PATH + "/api/resource/v1/cameras";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(cameras);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    public String indexCode(PreviewURLs previewURLs){
        String getDataApi = ARTEMIS_PATH + "/api/resource/v1/cameras/indexCode";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(previewURLs);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    //查询监控点列表V2
    public String search1(Cameras cameras){
        String getDataApi = ARTEMIS_PATH + "/api/resource/v2/camera/search";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(cameras);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    public String search(Cameras cameras){
        String getDataApi = ARTEMIS_PATH + "/api/resource/v2/encodeDevice/search";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(cameras);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    //获取视频设备信息
    public String P(PreviewURLs previewURLs) {
        String getDataApi = ARTEMIS_PATH + "/api/video/v2/cameras/previewURLs";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(previewURLs);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    //获取监控点回放取流URLv2
    public String back(PlaybackURLs playbackURLs) {
        String getDataApi = ARTEMIS_PATH + "/api/video/v2/cameras/playbackURLs";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(playbackURLs);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }
}
