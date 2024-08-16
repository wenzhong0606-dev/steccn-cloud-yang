package com.steccn.dp.consumer;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.dp.domain.DpColdEndCheckpointValue;
import com.steccn.dp.mapper.DpColdEndCheckpointValueMapper;
import com.steccn.dp.service.DpColdEndCheckpointValueService;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author AndrewLuo
 * @desc kafka消费者
 * @date 2023/12/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final DpColdEndCheckpointValueService dpColdEndCheckpointValueService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    /**
     * 消费监听
     *
     * @param record
     */
    @KafkaListener(topics = "cold_data", concurrency = "4")
    @Transactional(rollbackFor = Exception.class)
    public void tpFirst(ConsumerRecord<Object, String> record, Acknowledgment ack) {
        // 消费的哪个topic、partition的消息,打印出消息内容
//        log.info(Thread.currentThread().getId() + ":" + record.topic() + "-" + record.partition() + "-" + record.value());
        //对消息进行解析
        String key = record.key().toString();
        //如果是脉冲消息则直接修改redis
        String redisResult = stringRedisTemplate.opsForValue().get("CheckPoint::Value");
        List<String> list = JSONObject.parseObject(redisResult,List.class);
        if(!list.contains(key)){
            return;
        }
        Map<String, String> codeNameMap = FeignOptional.of(() -> remoteDictFeignApi.getCheckPointName(Sets.newHashSet(key)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //保存数据
        JSONObject value = JSONObject.parseObject(record.value());
        DpColdEndCheckpointValue model = new DpColdEndCheckpointValue();
        model.setCheckPointCode(key);
        model.setTimeStamp(LocalDateTime.parse(value.getString("serverTime"),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.setCheckPointName(codeNameMap.get(key));
        model.setValue(value.getBigDecimal("value"));
        model.setCreateDate(LocalDateTime.now());
        model.setUpdateDate(LocalDateTime.now());
        model.setCreateBy("kafkaConsumer");
        model.setUpdateBy("kafkaConsumer");
        dpColdEndCheckpointValueService.save(model);
        ack.acknowledge();
    }
}
