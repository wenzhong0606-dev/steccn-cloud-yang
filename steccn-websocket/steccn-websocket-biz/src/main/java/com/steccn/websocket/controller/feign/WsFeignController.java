package com.steccn.websocket.controller.feign;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.steccn.common.vo.R;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.service.WebSocketManager;
import com.steccn.websocket.service.WebSocketServer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@AllArgsConstructor
@RequestMapping("/feign/websocket")
public class WsFeignController {

    private final ObjectMapper objectMapper;

    @GetMapping("/push/oa")
    public R<MsgPushDTO> pushOa(@RequestParam("userId") Long userId) {
        MsgPushDTO msgPushDTO = new MsgPushDTO();
        msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.OA.getType());
        WebSocketManager.sendMessageByUser(userId, JSONUtil.toJsonStr(msgPushDTO));
        return new R<>(msgPushDTO);
    }

    @SneakyThrows
    @PostMapping("/push/msg")
    public R<MsgPushDTO> pushMsg(@RequestBody MsgPushDTO msgPushDTO) {
        if(ObjectUtil.isNull(msgPushDTO.getType())){
            msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
        }
        Set<Long> userIdList = msgPushDTO.getUserIdList();
        msgPushDTO.setUserIdList(null);
        String msg = objectMapper.writeValueAsString(msgPushDTO);
        userIdList.forEach(ele -> {
            WebSocketManager.sendMessageByUser(ele, msg);
        });
        return R.success(msgPushDTO);
    }
}