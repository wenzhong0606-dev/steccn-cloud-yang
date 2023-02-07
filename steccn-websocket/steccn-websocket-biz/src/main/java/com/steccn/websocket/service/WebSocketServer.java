package com.steccn.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author yequan
 * @desc websocket控制器 接口路径 ws://localhost:8087/webSocket/userId;
 */

@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {

    /**
     * 最长空闲时间-120s
     */
    private static final long MAX_IDLE_TIMEOUT = 120000L;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        try {
            this.session = session;
            session.setMaxIdleTimeout(MAX_IDLE_TIMEOUT);
            this.userId = userId;
            WebSocketManager.addWsServer(userId,this);
            log.info("【websocket消息】有新的连接，总数为:" + WebSocketManager.activeWsSize());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        try {
            WebSocketManager.removeWsServer(this.userId);
            log.info("【websocket消息】连接断开，总数为:" + WebSocketManager.activeWsSize());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端 uid = {},消息:{}", this.userId, message);
    }

    /**
     * 发送错误时的处理
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误 uid = {},原因:{}", this.userId, error.getMessage());
    }

    /**
     *
     * @return
     */
    public Session getSession() {
        return session;
    }
}
