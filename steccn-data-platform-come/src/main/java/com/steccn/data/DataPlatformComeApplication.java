package com.steccn.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * @author yequan
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableFeignClients(basePackages = "com.steccn")
public class DataPlatformComeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataPlatformComeApplication.class, args);
    }
}
