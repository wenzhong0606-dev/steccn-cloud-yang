package com.steccn.mes.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.dto.EqpListenerDTO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

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
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    /**
     * 消费监听
     *
     * @param record
     */
    @KafkaListener(topics = "opc_ua_topic_09", concurrency = "3")
    @Transactional(rollbackFor = Exception.class)
    public void tpFirst(ConsumerRecord<Object, String> record, Acknowledgment ack) {
        // 消费的哪个topic、partition的消息,打印出消息内容
        log.info(Thread.currentThread().getId() + ":" + record.topic() + "-" + record.partition() + "-" + record.value());
        //对消息进行解析
        EqpListenerDTO eqpListenerDTO = JSONObject.parseObject(record.value(), EqpListenerDTO.class);
        //获取消息对应的类型
        String redisResult = stringRedisTemplate.opsForValue().get("KafkaEqpSignal::" + eqpListenerDTO.getTagId());
        SysConfigurationDTO config = new SysConfigurationDTO();
        if(StrUtil.isNotBlank(redisResult)){
            config = JSONObject.parseObject(redisResult,SysConfigurationDTO.class);
        }else {
            config = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByContentAndType(new TupleDTO<>("KAFKA_EQP_SIGNAL", eqpListenerDTO.getTagId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if(Objects.isNull(config.getContent())){
                log.error(eqpListenerDTO.getTagId()+"不存在对应的设备");
                ack.acknowledge();
                throw new BusinessException("不存在tagId对应的设备");
            }
            stringRedisTemplate.opsForValue().set("KafkaEqpSignal::"+config.getContent(),JSONObject.toJSONString(config));
        }
        //找到对应关系
//        stringRedisTemplate.
//        MsgPushDTO msgPushDTO = JSONObject.parseObject(record.value(), MsgPushDTO.class);
//        //然后发送到websocket中
//        if (CollUtil.isNotEmpty(msgPushDTO.getUserIdList())) {
//            if (WebsocketConstant.WsPushLevelEnum.LEVEL_MORE.getType().equals(msgPushDTO.getPushLevel())) {
//                Set<Long> userIdList = msgPushDTO.getUserIdList();
//                msgPushDTO.setUserIdList(null);
//                WebSocketManager.sendMoreMessage(Lists.newArrayList(userIdList), JSONUtil.toJsonStr(msgPushDTO));
//            } else if (WebsocketConstant.WsPushLevelEnum.LEVEL_ONLINE_ALL.getType().equals(msgPushDTO.getPushLevel())) {
//                WebSocketManager.sendMessageToOnlineUser(record.value());
//            }
//        }
        ack.acknowledge();
    }
}
