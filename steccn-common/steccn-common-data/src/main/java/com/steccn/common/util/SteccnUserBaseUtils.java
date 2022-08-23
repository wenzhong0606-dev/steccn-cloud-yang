package com.steccn.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.steccn.common.vo.SteccnUserBaseInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 获取用户的基础信息
 */
public class SteccnUserBaseUtils {

    public static SteccnUserBaseInfo getUserBaseInfo(ServerHttpRequest request) {
        try {
            HttpHeaders headers = request.getHeaders();
            String accessToken = headers.getFirst("access_token");
            //校验token是否正确
            JWT jwt = JWTUtil.parseToken(accessToken);
            //获取用户名
            String sub = (String) jwt.getPayload("sub");
            SteccnUserBaseInfo steccnUserBaseInfo = new SteccnUserBaseInfo();
            steccnUserBaseInfo.setUserName(sub);
            steccnUserBaseInfo.setPermissionList(null);
            return steccnUserBaseInfo;
        } catch (Exception e) {
            throw new RuntimeException("解析token失败");
        }
    }

}
