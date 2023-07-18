package com.steccn.websocket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.service.WebSocketManager;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc websocket关闭
 * @date 2023/7/18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/websocket")
public class WsController {

    private final ObjectMapper objectMapper;

    @GetMapping("/close")
    public R<Boolean> close(@RequestParam("uuId") String uuId) throws JsonProcessingException {
        Long userId = SecurityUtils.getUser().getUserId();
        MsgPushDTO msgPushDTO = new MsgPushDTO();
        msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.CLOSE.getType());
        msgPushDTO.setContent(uuId);
        String closeMsg = objectMapper.writeValueAsString(msgPushDTO);
        WebSocketManager.sendMessageByUser(userId, closeMsg);
        return R.success(Boolean.TRUE);
    }
}
