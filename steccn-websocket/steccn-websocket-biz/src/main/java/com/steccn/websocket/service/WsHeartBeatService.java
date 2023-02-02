package com.steccn.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yequan
 * @desc 心跳检测，定时任务执行
 */
@Service
@Slf4j
public class WsHeartBeatService implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(() -> {
            WebSocketManager.sendPing(WebSocketManager.PING);
        }, 10, 30, TimeUnit.SECONDS);
    }
}
