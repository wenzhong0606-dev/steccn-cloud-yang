package com.steccn.websocket.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.websocket.service.WebSocketManager;
import com.steccn.websocket.service.WebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/feign/websocket")
public class WsFeignController {

    @GetMapping("/push/oa")
    public R<String> create(@RequestParam("userId") Long userId) {
        WebSocketManager.sendMessageByUser(userId, "OA");
        return new R<>("OA");
    }
}