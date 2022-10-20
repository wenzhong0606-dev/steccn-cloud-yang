package com.steccn.authserver.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author yequan
 */
@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //2,RequestContextHolder拿到刚进来的这个请求
                //RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //老请求
                //空指针Exception
                if (Objects.nonNull(requestAttributes)) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    if (request != null) {
                        //同步请求头数据,Cookie
                        String authorization = request.getHeader("Authorization");
                        //给新请求同步了老请求的Cookie
                        template.header("Authorization", authorization);
                    }
                }
            }
        };
    }
}