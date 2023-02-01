package com.steccn.websocket.service;

import lombok.extern.slf4j.Slf4j;
import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yequan
 * @desc websocket管理器
 */
@Slf4j
public class WebSocketManager {

    public static final String PING = "PING";

    /**
     * 存放在线用户的socket对象【包含用户id和连接】
     */
    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 添加websocket
     *
     * @param userId          用户
     * @param webSocketServer websocket
     */
    public static void addWsServer(Long userId, WebSocketServer webSocketServer) {
        webSocketMap.put(userId, webSocketServer);
    }

    /**
     * 删除ws的链接
     *
     * @param userId 用户id
     */
    public static void removeWsServer(Long userId) {
        webSocketMap.remove(userId);
    }

    /**
     * 活跃的websocket的数量，不是精确值
     *
     * @return 获取ws的数量
     */
    public static int activeWsSize() {
        return webSocketMap.size();
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
                    sendOneMessage(userId, message);
                    sb.append(userId);
                } catch (Exception e) {
                    webSocketMap.remove(userId);
                    log.info("userId = {} , 心跳断开");
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
    public static void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:" + message);
        for (WebSocketServer webSocket : webSocketMap.values()) {
            try {
                Session session = webSocket.getSession();
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
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
            Session session = webSocketServer.getSession();
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
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
                Session session = webSocketServer.getSession();
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }
}
