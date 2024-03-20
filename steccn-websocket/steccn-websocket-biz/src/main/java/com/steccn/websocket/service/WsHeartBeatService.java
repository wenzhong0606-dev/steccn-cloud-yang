package com.steccn.websocket.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.websocket.configuration.KafkaConf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yequan
 * @desc 心跳检测，定时任务执行
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WsHeartBeatService implements InitializingBean {

    private final ObjectMapper objectMapper;
    private final KafkaConf kafkaConf;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void afterPropertiesSet() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(() -> {
            WebSocketManager.sendPing(WebSocketManager.PING);
            //获取在线的人数 ，存放到redis中
            List<TupleDTO<Long, Long>> activeUserIdSet = WebSocketManager.activeWebSocket();
            //根据时间正序排列
            activeUserIdSet.sort(Comparator.comparing(TupleDTO::getField2));
            //将元素json格式化
            List<String> jsonList = Lists.newArrayList();
            activeUserIdSet.forEach(ele -> {
                try {
                    jsonList.add(objectMapper.writeValueAsString(ele));
                } catch (JsonProcessingException e) {
                    log.error("在线人数JSON 转换异常 -> {}", e.getCause());
                }
            });
            redisTemplate.delete("ACTIVE_WEBSOCKET_" + kafkaConf.getServerPort());
            if (CollUtil.isNotEmpty(jsonList)) {
                redisTemplate.opsForList().leftPushAll("ACTIVE_WEBSOCKET_" + kafkaConf.getServerPort(), jsonList);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }
}
