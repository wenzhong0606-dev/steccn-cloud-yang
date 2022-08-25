package com.steccn.gateway.filter;


import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.google.common.collect.Lists;
import com.steccn.common.constant.JwtConstants;
import com.steccn.common.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class ValidJwtTokenFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();
        log.info("#### 收到请求：{}", url);
        //1.白名单路由直接放行。
        if (inAllowRoutes(url)) {
            return chain.filter(exchange);
        }
        //2.获取token
        String token = exchange.getRequest().getHeaders().getFirst(JwtConstants.STR_ACCESS_TOKEN);
        if (StrUtil.isBlank(token)) {
            log.error("token is empty ...");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            //3.校验token是否正确
            JWT jwt = JWTUtil.parseToken(token);
            //3.1获取用户名
            String sub = (String) jwt.getPayload(JwtConstants.STR_SUB);
            log.info("用户名 ->{}", sub);
            //3.2校验用户传过来的userId是否为当前jwt中解析出来的userId，用于安全验证

        } catch (Exception e) {
            log.error("{}", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        Mono<Void> filter = chain.filter(exchange);
        return filter;
    }


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * 方法说明 检查传参是否在路由白名单中
     *
     * @param route
     * @return
     * @author Jinglihui
     * @date 2022/7/9 15:02
     */
    private boolean inAllowRoutes(String route) {
        //获取token无需走认证逻辑
        List<String> list = Lists.newArrayList();
        return false;
    }
}
