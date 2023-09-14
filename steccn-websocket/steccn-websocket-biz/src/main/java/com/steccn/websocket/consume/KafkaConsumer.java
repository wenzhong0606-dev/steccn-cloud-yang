package com.steccn.websocket.consume;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.service.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author yequan
 * @desc kafka消费者
 * @date 2023/9/13
 */
@Service
@Slf4j
public class KafkaConsumer {

    /**
     * 消费监听
     *
     * @param record
     */
    @KafkaListener(topics = "${topicWs}", concurrency = "3")
    public void tpFirst(ConsumerRecord<Object, String> record) {
        // 消费的哪个topic、partition的消息,打印出消息内容
        log.info(Thread.currentThread().getId() + ":" + record.topic() + "-" + record.partition() + "-" + record.value());
        //对消息进行解析
        MsgPushDTO msgPushDTO = JSONObject.parseObject(record.value(), MsgPushDTO.class);
        //然后发送到websocket中
        if (CollUtil.isNotEmpty(msgPushDTO.getUserIdList())) {
            if(WebsocketConstant.WsPushLevelEnum.LEVEL_MORE.getType().equals(msgPushDTO.getPushLevel())){
                Set<Long> userIdList = msgPushDTO.getUserIdList();
                msgPushDTO.setUserIdList(null);
                WebSocketManager.sendMoreMessage(Lists.newArrayList(userIdList), JSONUtil.toJsonStr(msgPushDTO));
            }else if(WebsocketConstant.WsPushLevelEnum.LEVEL_ONLINE_ALL.getType().equals(msgPushDTO.getPushLevel())){
                WebSocketManager.sendMessageToOnlineUser(record.value());
            }
        }
    }
}
