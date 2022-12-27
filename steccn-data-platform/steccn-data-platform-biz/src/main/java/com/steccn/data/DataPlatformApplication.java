package com.steccn.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@EnableFeignClients(basePackages = "com.steccn")
@EnableScheduling
public class DataPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataPlatformApplication.class, args);
    }
}
