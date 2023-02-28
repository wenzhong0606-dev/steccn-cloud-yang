package com.steccn.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.steccn.platform.dto.SysMobilePushDTO;
import com.steccn.platform.service.SysMobilePushService;
import com.steccn.platform.service.SysUserClientRefService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author yequan
 * @desc 手机消息推送
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysMobilePushServiceImpl implements SysMobilePushService {

    private final SysUserClientRefService sysUserClientRefService;

    @Override
    public boolean push(SysMobilePushDTO dto) {
        //根据用户id查询clientId
        String clientId = sysUserClientRefService.lastClientId(dto.getUserId());
        if (StrUtil.isNotBlank(clientId)) {
            dto.setPush_clientid(clientId);
            dto.setForce_notification(Boolean.TRUE);
            dto.setRequest_id(UUID.randomUUID().toString().replaceAll("-", ""));
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(dto));
            Request request = new Request.Builder()
                    .url("https://fc-mp-921e670b-6809-4f1c-9538-76bc3476034d.next.bspapp.com/smartfactory")
                    .method("GET", body)
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://www.apifox.cn)")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "*/*")
                    .addHeader("Host", "fc-mp-921e670b-6809-4f1c-9538-76bc3476034d.next.bspapp.com")
                    .addHeader("Connection", "keep-alive")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                log.debug("response {}", response);
            } catch (Exception e) {
                log.error("异常原因 -> {}", e.getLocalizedMessage());
            }
        }

        return Boolean.TRUE;
    }
}
