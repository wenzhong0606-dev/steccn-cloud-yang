package com.steccn.authserver.config;

import com.google.common.collect.Maps;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author yequan
 * Oauth2异常信息返回处理
 */
@Slf4j
public class AuthenticationForbiddenEntryPoint implements AccessDeniedHandler {

    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException authException)
            throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("code", "0");
        map.put("msg", "暂无权限");
        map.put("data", null);
        response.getWriter().write(JSONObject.toJSONString(map));
    }
}
