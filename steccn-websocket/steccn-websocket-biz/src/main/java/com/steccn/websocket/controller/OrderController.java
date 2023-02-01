package com.steccn.websocket.controller;

import com.steccn.websocket.service.WebSocket;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class OrderController {
    @Resource
    WebSocket webSocket;

    @RequestMapping("/create")
    public Object create(@RequestParam("userId") String userId) {
        webSocket.sendOneMessage(userId, "收到一个新的订单");
        return null;
    }
}