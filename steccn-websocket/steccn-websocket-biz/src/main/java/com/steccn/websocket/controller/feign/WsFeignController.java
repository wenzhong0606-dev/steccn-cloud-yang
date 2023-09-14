package com.steccn.websocket.controller.feign;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.steccn.common.vo.R;
import com.steccn.websocket.configuration.KafkaConf;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/feign/websocket")
@Slf4j
public class WsFeignController {

    private final KafkaTemplate kafkaTemplate;
    private final KafkaConf kafkaConf;

    @GetMapping("/push/oa")
    public R<MsgPushDTO> pushOa(@RequestParam("userId") Long userId) {
        MsgPushDTO msgPushDTO = new MsgPushDTO();
        msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.OA.getType());
        msgPushDTO.setUserIdList(Sets.newHashSet(userId));
        msgPushDTO.setPushLevel(WebsocketConstant.WsPushLevelEnum.LEVEL_MORE.getType());
//        WebSocketManager.sendMessageByUser(userId, JSONUtil.toJsonStr(msgPushDTO));
        kafkaTemplate.send(kafkaConf.getTopicWs(), JSONObject.toJSONString(msgPushDTO)).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("发送消息失败：" + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("发送消息成功：" + result.getRecordMetadata().topic() + "-"
                        + result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset());
            }
        });
        return new R<>(msgPushDTO);
    }

    @SneakyThrows
    @PostMapping("/push/msg")
    public R<MsgPushDTO> pushMsg(@RequestBody MsgPushDTO msgPushDTO) {
//        Set<Long> userIdList = msgPushDTO.getUserIdList();
//        msgPushDTO.setUserIdList(null);
//        String msg = objectMapper.writeValueAsString(msgPushDTO);
//        userIdList.forEach(ele -> {
//            WebSocketManager.sendMessageByUser(ele, msg);
//        });
        msgPushDTO.setPushLevel(WebsocketConstant.WsPushLevelEnum.LEVEL_MORE.getType());
        kafkaTemplate.send(kafkaConf.getTopicWs(), JSONObject.toJSONString(msgPushDTO)).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("发送消息失败：" + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("发送消息成功：" + result.getRecordMetadata().topic() + "-"
                        + result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset());
            }
        });
        return R.success(msgPushDTO);
    }

    @ApiOperation("给指定的人推送刷新消息")
    @PostMapping("/push/refresh")
    public R<MsgPushDTO> pushRefreshMsg(@RequestBody MsgPushDTO msgPushDTO) {

//        msgPushDTO.setType(msgPushDTO.getType());
        msgPushDTO.setPushLevel(WebsocketConstant.WsPushLevelEnum.LEVEL_ONLINE_ALL.getType());
        kafkaTemplate.send(kafkaConf.getTopicWs(), JSONObject.toJSONString(msgPushDTO)).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("发送消息失败：" + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("发送消息成功：" + result.getRecordMetadata().topic() + "-"
                        + result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset());
            }
        });
        return R.success(msgPushDTO);
//        WebSocketManager.sendMessageToOnlineUser(JSONUtil.toJsonStr(msgPushDTO));
//        return new R<>(msgPushDTO);
    }
}