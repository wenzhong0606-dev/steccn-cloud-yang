package com.steccn.websocket.controller;

import com.steccn.common.vo.R;
import com.steccn.websocket.service.WebSocketManager;
import com.steccn.websocket.service.WebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/websocket")
public class TestController {

    @GetMapping("/create")
    public R<String> create(@RequestParam("userId") Long userId) {
        WebSocketManager.sendOneMessage(userId, "收到一个新的订单");
        return new R<>("前端收到一个信号");
    }
}