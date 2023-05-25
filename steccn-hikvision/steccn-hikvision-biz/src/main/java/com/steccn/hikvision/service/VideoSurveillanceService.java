package com.steccn.hikvision.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.steccn.hikvision.api.VideoApi;
import com.steccn.hikvision.entity.PreviewURLs;


import java.io.IOException;

public class VideoSurveillanceService {
    public void v(String message) throws IOException {
        JSONObject transition = JSON.parseObject(message);
        String value = (String) transition.get("视频监控区域名称");

        JSONObject jsonObject = new JSONObject();
        JSONObject xinxi = new JSONObject();

        JSONArray Array = new JSONArray();
        jsonObject.put("instruction", "shipinjiankong");
        if (value.equals("D56")) {
            xinxi.put("监控点名称", "D56-1号库区中间通道");
            xinxi.put("监控点唯一标识", "0d72e2b9c4504895aedd31e714baf170");
            xinxi.put("路径", getUrl(2, "0d72e2b9c4504895aedd31e714baf170"));
            Array.add(xinxi);
        }
    }


    private String getUrl(int number, String sole) {
        String urlSole = "";
        if (number == 1) {
            VideoApi videoApi = new VideoApi();
            PreviewURLs previewURLs = new PreviewURLs();
            previewURLs.setCameraIndexCode(sole);
            previewURLs.setStreamType(0);
            previewURLs.setProtocol("hls");
            previewURLs.setTransmode(1);
            previewURLs.setExpand("transcode=0");
            previewURLs.setStreamform("ps");
            String url = videoApi.P(previewURLs);
            JSONObject dizhi = JSONObject.parseObject(url);
            urlSole = ((JSONObject) dizhi.get("data")).get("url").toString();
        }
        return urlSole;
    }
}
