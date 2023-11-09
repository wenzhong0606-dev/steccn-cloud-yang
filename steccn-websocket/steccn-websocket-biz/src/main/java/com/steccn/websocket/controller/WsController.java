package com.steccn.websocket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.websocket.configuration.KafkaConf;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc websocket关闭
 * @date 2023/7/18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/websocket")
@Slf4j
public class WsController {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate kafkaTemplate;
    private final KafkaConf kafkaConf;

    @GetMapping("/close")
    public R<Boolean> close(@RequestParam("uuId") String uuId) throws JsonProcessingException {
        Long userId = SecurityUtils.getUser().getUserId();
        MsgPushDTO msgPushDTO = new MsgPushDTO();
        msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.CLOSE.getType());
        msgPushDTO.setContent(uuId);
        msgPushDTO.setUserIdList(Sets.newHashSet(userId));
        String closeMsg = objectMapper.writeValueAsString(msgPushDTO);
        kafkaTemplate.send(kafkaConf.getTopicWs(), objectMapper.writeValueAsString(msgPushDTO)).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("发送消息失败：" + ex.getMessage());
            }
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("发送消息成功：" + result.getRecordMetadata().topic() + "-"
                        + result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset());
            }
        });
        //投递消息到kafka
        return R.success(Boolean.TRUE);
    }
}