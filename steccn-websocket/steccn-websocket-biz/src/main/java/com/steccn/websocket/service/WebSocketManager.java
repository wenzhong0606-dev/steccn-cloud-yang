package com.steccn.websocket.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;

import javax.websocket.Session;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author yequan
 * @desc websocket管理器
 */
@Slf4j
public class WebSocketManager {

    /**
     * 发送心跳的内容
     */
    public static final String PING = "PING";

    /**
     * 存放在线用户的socket对象【包含用户id-随机数】
     */
    private static ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketServer>> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 添加websocket
     *
     * @param userId          用户
     * @param webSocketServer websocket
     */
    public static void addWsServer(Long userId, WebSocketServer webSocketServer) {
        webSocketMap.compute(userId, (k, v) -> {
            if (CollUtil.isEmpty(v)) {
                v = new CopyOnWriteArraySet<>();
            }
            v.add(webSocketServer);
            return v;
        });
    }

    /**
     * 删除ws的链接
     *
     * @param userId 用户id
     */
    public static void removeWsServer(Long userId, WebSocketServer webSocketServer) {
        webSocketMap.compute(userId, (k, v) -> {
            if (CollUtil.isNotEmpty(v)) {
                v.remove(webSocketServer);
            }
            return v;
        });
    }

    /**
     * 活跃的websocket的数量，不是精确值
     *
     * @return 获取ws的数量
     */
    public synchronized static int activeWsSize() {
        int sum = 0;
        for (Map.Entry<Long, CopyOnWriteArraySet<WebSocketServer>> entry : webSocketMap.entrySet()) {
            sum += entry.getValue().size();
        }
        return sum;
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
            for (Map.Entry<Long, CopyOnWriteArraySet<WebSocketServer>> entry : webSocketMap.entrySet()) {
                for (WebSocketServer webSocketServer : entry.getValue()) {
                    try {
                        sendOneMessage(webSocketServer, message);
                        sb.append(webSocketServer.getUserId()).append(",");
                    } catch (Exception e) {
                        entry.getValue().remove(webSocketServer);
                        log.debug("uid = {} , 心跳断开");
                        log.error(e.getLocalizedMessage());
                    }
                }
            }
            log.debug("客户端心跳检测结果: " + sb + "连接正在运行");
        }
    }

    /**
     * 广播消息
     *
     * @param message 消息内容
     */
    public static void sendAllMessage(String message) {
        log.debug("【websocket消息】广播消息:" + message);
        for (Map.Entry<Long, CopyOnWriteArraySet<WebSocketServer>> entry : webSocketMap.entrySet()) {
            for (WebSocketServer webSocketServer : entry.getValue()) {
                try {
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

    /**
     * 单播消息
     *
     * @param userId  用户id
     * @param message 消息内容
     */
    public static void sendMessageByUser(Long userId, String message) {
        log.debug("【websocket消息】 单点 用户：{}  消息: {}", userId, message);
        try {
            CopyOnWriteArraySet<WebSocketServer> webSocketServers = webSocketMap.get(userId);
            for (WebSocketServer webSocketServer : webSocketServers) {
                sendOneMessage(webSocketServer, message);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * 给在线用户发送消息
     *
     * @param message 消息内容
     */
    public static void sendMessageToOnlineUser(String message) {
        try {
            for(Map.Entry<Long, CopyOnWriteArraySet<WebSocketServer>> entry:webSocketMap.entrySet()){
                log.debug("【websocket消息】 单点 用户：{}  消息: {}", entry.getKey(), message);
                CopyOnWriteArraySet<WebSocketServer> webSocketServers = entry.getValue();
                for (WebSocketServer webSocketServer : webSocketServers) {
                    sendOneMessage(webSocketServer, message);
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }


    /**
     * 单播消息
     *
     * @param webSocketServer 消息对象
     * @param message         消息内容
     */
    public static void sendOneMessage(WebSocketServer webSocketServer, String message) {
        log.debug("【websocket消息】 单点 用户：{}  消息: {}", webSocketServer.getUserId(), message);
        try {
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
        log.debug("【websocket消息】 组播消息:" + message);
        for (Long userId : userIdList) {

            CopyOnWriteArraySet<WebSocketServer> webSocketServers = webSocketMap.get(userId);
            for (WebSocketServer webSocketServer : webSocketServers) {
                try {
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
}
