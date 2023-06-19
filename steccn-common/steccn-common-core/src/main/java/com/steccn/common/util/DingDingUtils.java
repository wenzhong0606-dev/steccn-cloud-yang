package com.steccn.common.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.codec.Base64;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 钉钉工具类
 * @date 2023/6/19
 */
@Slf4j
public class DingDingUtils {

    /**
     * 群里面发送消息
     *
     * @param content    消息内容
     * @param isAtAll    是否@所有人
     * @param mobileList 被@人的手机号
     * @param userIdList 被@人的用户userid
     * @throws Exception
     */
    public static void sendTextMsg(String content, List<String> mobileList, List<String> userIdList, boolean isAtAll) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqTextStr(content, isAtAll, mobileList, userIdList);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-text类型
     *
     * @param content    消息内容
     * @param isAtAll    是否@所有人
     * @param mobileList 被@人的手机号
     * @param atUserIds  被@人的用户userid
     * @return
     */
    public static String buildReqTextStr(String content, boolean isAtAll, List<String> mobileList, List<String> atUserIds) {
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);

        Map<String, Object> atMap = Maps.newHashMap();
        atMap.put("isAtAll", isAtAll);
        atMap.put("atMobiles", mobileList);
        atMap.put("atUserIds", atUserIds);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);

        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 群里面发送消息
     *
     * @param title      消息标题
     * @param messageUrl 点击消息跳转的URL
     * @param picUrl     图片URL
     * @param text       消息内容
     * @throws Exception
     */
    public static void sendLinkMsg(String title, String text, String messageUrl, String picUrl) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqLinkStr(title, text, messageUrl, picUrl);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-link类型
     *
     * @param title      消息标题
     * @param text       消息内容
     * @param messageUrl 点击消息跳转的URL
     * @param picUrl     图片URL
     * @return
     */
    private static String buildReqLinkStr(String title, String text, String messageUrl, String picUrl) {
        Map<String, String> linkMap = Maps.newHashMap();
        linkMap.put("text", text);
        linkMap.put("title", title);
        linkMap.put("picUrl", picUrl);
        linkMap.put("messageUrl", messageUrl);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "link");
        reqMap.put("link", linkMap);

        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 群里面发送消息
     *
     * @param title      首屏会话透出的展示内容
     * @param text       markdown格式的消息
     * @param isAtAll    是否@所有人
     * @param mobileList 被@人的手机号
     * @param atUserIds  被@人的用户userid
     * @throws Exception
     */
    public static void sendMarkdownMsg(String title, String text, List<String> mobileList, List<String> atUserIds, boolean isAtAll) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqMarkdownStr(title, text, mobileList, atUserIds, isAtAll);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-markdown类型
     *
     * @param title      首屏会话透出的展示内容
     * @param text       markdown格式的消息
     * @param isAtAll    是否@所有人
     * @param mobileList 被@人的手机号
     * @param atUserIds  被@人的用户userid
     * @return
     */
    private static String buildReqMarkdownStr(String title, String text, List<String> mobileList, List<String> atUserIds, boolean isAtAll) {
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("title", title);
        contentMap.put("text", text);

        Map<String, Object> atMap = Maps.newHashMap();
        atMap.put("isAtAll", isAtAll);
        atMap.put("atMobiles", mobileList);
        atMap.put("atUserIds", atUserIds);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "markdown");
        reqMap.put("markdown", contentMap);
        reqMap.put("at", atMap);

        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 群里面发送消息
     *
     * @param title          首屏会话透出的展示内容
     * @param text           markdown格式的消息
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @param singleTitle    单个按钮的标题
     * @param singleURL      点击消息跳转的URL
     * @throws Exception
     */
    public static void sendActionCardMsg(String title, String text, String btnOrientation, String singleTitle, String singleURL) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqActionCard(title, text, btnOrientation, singleTitle, singleURL);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-整体跳转ActionCard类型
     *
     * @param title          首屏会话透出的展示内容
     * @param text           markdown格式的消息
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @param singleTitle    单个按钮的标题
     * @param singleURL      点击消息跳转的URL
     * @return
     */
    private static String buildReqActionCard(String title, String text, String btnOrientation, String singleTitle, String singleURL) {
        Map<String, String> actionCardMap = Maps.newHashMap();
        actionCardMap.put("title", title);
        actionCardMap.put("text", text);
        actionCardMap.put("btnOrientation", btnOrientation);
        actionCardMap.put("singleTitle", singleTitle);
        actionCardMap.put("singleURL", singleURL);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "actionCard");
        reqMap.put("actionCard", actionCardMap);

        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 群里面发送消息
     *
     * @param title          首屏会话透出的展示内容
     * @param text           markdown格式的消息
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @param btnsList       按钮
     * @throws Exception
     */
    public static void sendActionCardMsg(String title, String text, String btnOrientation, List<Map<String, String>> btnsList) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqActionCard(title, text, btnOrientation, btnsList);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-独立跳转ActionCard类型
     *
     * @param title          首屏会话透出的展示内容
     * @param text           markdown格式的消息
     * @param btnOrientation 0：按钮竖直排列 1：按钮横向排列
     * @param btnsList       按钮
     * @return
     */
    private static String buildReqActionCard(String title, String text, String btnOrientation, List<Map<String, String>> btnsList) {
        Map<String, Object> actionCardMap = Maps.newHashMap();
        actionCardMap.put("title", title);
        actionCardMap.put("text", text);
        actionCardMap.put("btnOrientation", btnOrientation);
        actionCardMap.put("btns", btnsList);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "actionCard");
        reqMap.put("actionCard", actionCardMap);

        return JSONObject.toJSONString(reqMap);
    }


    /**
     * 群里面发送消息
     *
     * @param linksList 列表集合
     * @throws Exception
     */
    public static void sendFeedCardMsg(List<Map<String, String>> linksList) throws Exception {
        String dingUrl = getDingUrl();
        // 组装请求内容
        String reqStr = buildReqFeedCard(linksList);
        // 推送消息（http请求）
        //由于用到的是公司网络需要配置代理才能连接外网，不需要代理的可以删除HttpProxy设置
        String result = HttpRequest.post(dingUrl).body(reqStr).execute().body();
        log.info("钉钉请求发送成功，返回结果：" + result);
    }

    /**
     * 组装请求报文-FeedCard类型
     *
     * @param linksList 列表集合
     * @return
     */
    private static String buildReqFeedCard(List<Map<String, String>> linksList) {
        Map<String, Object> feedCardMap = Maps.newHashMap();
        feedCardMap.put("links", linksList);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "feedCard");
        reqMap.put("feedCard", feedCardMap);

        return JSONObject.toJSONString(reqMap);
    }

    /**
     * 获取请求url
     *
     * @return 钉钉机器人地址
     */
    private static String getDingUrl() throws Exception {
        // 获取系统时间戳
        Long timestamp = System.currentTimeMillis();
        String SECRET = "SEC111cbf5ce9a58e87a50344abbd83c8e1383394bf8b02e047a09528ad18332dcb";
        String WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=5a860fa32f83efd5359f6a0eb0f174adbe01c66946be5e80cd504f956db9edde";
        // 拼接
        String stringToSign = timestamp + "\n" + SECRET;
        // 使用HmacSHA256算法计算签名
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        // 进行Base64 encode 得到最后的sign，可以拼接进url里
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        // 钉钉机器人地址（配置机器人的webhook），为了让每次请求不同，避免钉钉拦截，加上时间戳
        String dingUrl = WEBHOOK + "&timestamp=" + timestamp + "&sign=" + sign;
        return dingUrl;
    }
}
