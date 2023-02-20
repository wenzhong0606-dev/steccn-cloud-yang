package com.steccn.websocket.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;

import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
     * 分割标识
     */
    public static final char STR_SPLIT = '-';

    public static final List<Integer> END_POINT_LIST = Lists.newArrayList(0, 1);


    /**
     * 存放在线用户的socket对象【包含用户id-端标识和连接】
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 添加websocket
     *
     * @param userId          用户
     * @param webSocketServer websocket
     */
    public static void addWsServer(Long userId, Integer endPoint, WebSocketServer webSocketServer) {
        webSocketMap.put(jointUserId(userId, endPoint), webSocketServer);
    }

    /**
     * 删除ws的链接
     *
     * @param userId 用户id
     */
    public static void removeWsServer(Long userId, Integer endPoint) {
        webSocketMap.remove(jointUserId(userId, endPoint));
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
            webSocketMap.forEach((uid, wsServer) -> {
                try {
                    Long userId = extractUserId(uid);
                    sendOneMessage(uid, message);
                    sb.append(userId).append(",");
                } catch (Exception e) {
                    webSocketMap.remove(uid);
                    log.debug("uid = {} , 心跳断开");
                    log.error(e.getLocalizedMessage());
                }
            });
            log.debug("客户端心跳检测结果: " + sb + "连接正在运行");
        }
    }


    /**
     * 抽取用户id
     *
     * @param uid 用户id + 端标识
     * @return 用户id
     */
    private static Long extractUserId(String uid) {
        String[] split = Strings.split(uid, STR_SPLIT);
        return Long.parseLong(split[0]);
    }


    /**
     * 拼接用户id + 端标识
     *
     * @param userId   用户id
     * @param endPoint
     * @return
     */
    private static String jointUserId(Long userId, Integer endPoint) {
        return userId + String.valueOf(STR_SPLIT) + endPoint;
    }


    /**
     * 广播消息
     *
     * @param message 消息内容
     */
    public static void sendAllMessage(String message) {
        log.debug("【websocket消息】广播消息:" + message);
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
    public static void sendMessageByUser(Long userId, String message) {
        log.debug("【websocket消息】 单点 用户：{}  消息: {}", userId, message);
        try {
            List<String> uidList = Lists.newArrayList();
            END_POINT_LIST.forEach(ele -> {
                uidList.add(jointUserId(userId, ele));
            });
            sendMoreMessage(uidList, message);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }


    /**
     * 单播消息
     *
     * @param uid     用户id
     * @param message 消息内容
     */
    public static void sendOneMessage(String uid, String message) {
        log.debug("【websocket消息】 单点 用户：{}  消息: {}", uid, message);
        try {
            WebSocketServer webSocketServer = webSocketMap.get(uid);
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
     * @param uidList 用户id集合
     * @param message 消息内容
     */
    public static void sendMoreMessage(List<String> uidList, String message) {
        log.debug("【websocket消息】 组播消息:" + message);
        for (String uid : uidList) {
            try {
                WebSocketServer webSocketServer = webSocketMap.get(uid);
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
