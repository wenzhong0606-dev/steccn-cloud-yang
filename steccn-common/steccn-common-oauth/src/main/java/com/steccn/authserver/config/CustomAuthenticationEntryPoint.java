package com.steccn.authserver.config;

import com.google.common.collect.Maps;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author yequan
 * Oauth2异常信息返回处理
 */
@Slf4j
public class CustomAuthenticationEntryPoint extends OAuth2AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.error(e.getMessage());
        //如果是client_id和client_secret相关异常 返回自定义的数据格式
        if (e instanceof BadCredentialsException) {
            res(response);
        } else if (e instanceof InsufficientAuthenticationException) {
            res(response);
        } else {
            super.commence(request, response, e);
        }
    }

    private void res(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("code", "0");
        map.put("msg", "暂无权限");
        map.put("data", null);
        response.getWriter().write(JSONObject.toJSONString(map));
    }
}
