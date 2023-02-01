package com.steccn.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * @desc websocket服务
 * @author yequan
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableCaching
@EnableFeignClients(basePackages = "com.steccn")
@EnableWebSocket
public class WebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketApplication.class, args);
    }
}
