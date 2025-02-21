package com.steccn.hikvision.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.steccn.hikvision.dto.SysHikEventDTO;
import com.steccn.hikvision.dto.SysHikPageDTO;
import com.steccn.hikvision.entity.Cameras;
import com.steccn.hikvision.entity.PlaybackURLs;
import com.steccn.hikvision.entity.PreviewURLs;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;


/**
 * Auto Create on 2021-10-15 02:26:00
 */
@Component
@Data
public class VideoApi {

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

    public String indexCode(PreviewURLs previewURLs) {
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
    public String search1(Cameras cameras) {
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

    public String search(Cameras cameras) {
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

    //获取监测点预览流
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

    public String bookEvent(SysHikEventDTO dto) {
        String getDataApi = ARTEMIS_PATH + "/api/eventService/v1/eventSubscriptionByEventTypes";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(dto);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }

    /**
     * 单点登录
     * @param querys
     * @return
     */
    public String ssoLogin(Map<String,String> querys) {
        String getDataApi = ARTEMIS_PATH + "/api/cas/v1/tgt/login";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String result = ArtemisHttpUtil.doGetArtemis(path,querys,null,"application/json");
        return result;
    }

    /**
     * 单点登录
     * @param querys
     * @return
     */
    public String getCompanyInfo(Map<String,String> querys) {
        String getDataApi = ARTEMIS_PATH + "/api/open/v1/allType/companyQuery";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String result = ArtemisHttpUtil.doGetArtemis(path,querys,null,"application/json");
        return result;
    }

    /**
     * 查询榜单URL
     * @param 称重流程单号
     * @return
     */
    public String queryWeighUrl(String weighNo) {
        Map<String,String> querys = Maps.newHashMap();
        querys.put("weighNo",weighNo);
        String getDataApi = ARTEMIS_PATH + "/api/open/v2/wreg/weigh/getWeighRegUrl";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String result = ArtemisHttpUtil.doGetArtemis(path,querys,null,"application/json");
        return result;
    }

    /**
     * 获取停车场内车辆
     * @param SysHikPageDTO
     * @return
     */
    public String searchCars(SysHikPageDTO dto) {
        String getDataApi = ARTEMIS_PATH + "/api/pms/v1/tempCarInRecords/page";
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", getDataApi);
            }
        };
        String body = JSON.toJSONString(dto);
        String result = ArtemisHttpUtil.doPostStringArtemis(path, body, null, null, "application/json");
        return result;
    }
}
