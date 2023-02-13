package com.steccn.oa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableFeignClients(basePackages = "com.steccn")
@EnableCaching
public class SteccnOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteccnOaApplication.class, args);
    }
}
