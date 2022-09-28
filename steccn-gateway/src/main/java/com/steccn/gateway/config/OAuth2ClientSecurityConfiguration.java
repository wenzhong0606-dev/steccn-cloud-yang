package com.steccn.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 类说明
 * gateway 是基于WebFlux的，所有使用MVC的配置方法都无效
 *
 * @author Jinglihui
 * @date 2022/7/27
 */
@EnableWebFluxSecurity
public class OAuth2ClientSecurityConfiguration {
    @Bean
    SecurityWebFilterChain oauth2ClientSecurityFilterChain(ServerHttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .oauth2Client();
        return httpSecurity.build();
    }

}
