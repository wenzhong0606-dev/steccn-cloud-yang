package com.steccn.gateway.filter;


import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
        String token = exchange.getRequest().getHeaders().getFirst("access_token");
        if (StrUtil.isBlank(token)) {
            log.error("token is empty ...");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            //3.校验token是否正确
            JWT jwt = JWTUtil.parseToken(token);
            //3.1获取用户名
            String sub = (String) jwt.getPayload("sub");
            //3.2存放到threadlocal中去，但threadlocal需要释放，避免内存泄漏
            log.info("用户名 ->{}", sub);
        } catch (Exception e) {
            log.error("{}", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return -1000;
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
        return false;
    }
}
