package com.steccn.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final long MAX_IDLE_TIMEOUT = 10000L;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 存放在线用户的socket对象【包含用户id和连接】
     */
    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        try {
            this.session = session;
            session.setMaxIdleTimeout(MAX_IDLE_TIMEOUT);
            this.userId = userId;
            webSocketMap.put(userId, this);
            log.info("【websocket消息】有新的连接，总数为:" + webSocketMap.size());
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
            webSocketMap.remove(this.userId);
            log.info("【websocket消息】连接断开，总数为:" + webSocketMap.size());
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
     * 服务端群发消息-心跳包
     *
     * @param message 心跳消息 一般为PING
     * @return
     */
    public static synchronized void sendPing(String message) {
        //集合中存在有的
        if (webSocketMap.size() > 0) {
            StringBuffer sb = new StringBuffer();
            webSocketMap.forEach((userId, wsServer) -> {
                try {
                    wsServer.sendOneMessage(userId, message);
                } catch (Exception e) {
                    webSocketMap.remove(userId);
                    log.error(e.getLocalizedMessage());
                }
            });
            log.info("客户端心跳检测结果: " + sb + "连接正在运行");
        }
    }


    /**
     * 广播消息
     *
     * @param message 消息内容
     */
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:" + message);
        for (WebSocketServer webSocket : webSocketMap.values()) {
            try {
                if (webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * 单播消息
     *
     * @param userId  用户id
     * @param message 消息内容
     */
    public static void sendOneMessage(Long userId, String message) {
        log.info("【websocket消息】 单点 用户：{}  消息: {}", userId, message);
        try {
            WebSocketServer webSocketServer = webSocketMap.get(userId);
            if (webSocketServer.session.isOpen()) {
                webSocketServer.session.getAsyncRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 多播消息，组播消息
     *
     * @param userIdList 用户id集合
     * @param message    消息内容
     */
    public static void sendMoreMessage(List<Long> userIdList, String message) {
        log.info("【websocket消息】 组播消息:" + message);
        for (Long userId : userIdList) {
            try {
                WebSocketServer webSocketServer = webSocketMap.get(userId);
                if (webSocketServer.session.isOpen()) {
                    webSocketServer.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }
}
