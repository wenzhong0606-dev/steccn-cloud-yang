package com.steccn.data.service.impl;

import com.steccn.data.service.EpMeterCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yequan
 * @desc 最近抄表时间服务
 * @date 2023/3/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LastRecordTimeServiceImpl implements InitializingBean {

    private final EpMeterCollectService epMeterCollectService;

    @Override
    public void afterPropertiesSet() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            LocalDateTime lastRecordTime = epMeterCollectService.getLastRecordTime();
            epMeterCollectService.refreshLastReadTime(lastRecordTime);
        }, 30, 400, TimeUnit.SECONDS);
    }
}
