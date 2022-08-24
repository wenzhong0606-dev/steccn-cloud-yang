package com.steccn.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.steccn.common.constant.JwtConstants;
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
            String accessToken = headers.getFirst(JwtConstants.STR_ACCESS_TOKEN);
            //不含有token则直接返回空
            if (StrUtil.isBlank(accessToken)) {
                return null;
            }
            //校验token是否正确
            JWT jwt = JWTUtil.parseToken(accessToken);
            //获取用户名
            String sub = (String) jwt.getPayload(JwtConstants.STR_SUB);
            SteccnUserBaseInfo steccnUserBaseInfo = new SteccnUserBaseInfo();
            steccnUserBaseInfo.setUserName(sub);
            steccnUserBaseInfo.setPermissionList(null);
            return steccnUserBaseInfo;
        } catch (Exception e) {
            throw new RuntimeException("解析token失败");
        }
    }

}
