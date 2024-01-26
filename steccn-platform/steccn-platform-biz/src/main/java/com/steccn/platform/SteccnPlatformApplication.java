package com.steccn.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * @author yequan
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableScheduling
@EnableCaching
@EnableFeignClients(basePackages = "com.steccn")
public class SteccnPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteccnPlatformApplication.class, args);
    }
}
