package com.steccn.websocket.controller.feign;

import cn.hutool.json.JSONUtil;
import com.steccn.common.vo.R;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.service.WebSocketManager;
import com.steccn.websocket.service.WebSocketServer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/feign/websocket")
public class WsFeignController {

    @GetMapping("/push/oa")
    public R<String> pushOa(@RequestParam("userId") Long userId) {
        WebSocketManager.sendMessageByUser(userId, "OA");
        return new R<>("OA");
    }

    @PostMapping("/push/msg")
    public R<MsgPushDTO> pushMsg(@RequestBody MsgPushDTO msgPushDTO) {
        String msg = JSONUtil.toJsonStr(msgPushDTO);
        msgPushDTO.getUserIdList().forEach(ele -> {
            WebSocketManager.sendMessageByUser(ele, msg);
        });
        return R.success(msgPushDTO);
    }
}