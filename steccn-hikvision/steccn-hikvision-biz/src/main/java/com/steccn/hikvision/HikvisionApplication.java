package com.steccn.hikvision;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOpenApi
@EnableFeignClients(basePackages = "com.steccn")
public class HikvisionApplication {
    public static void main(String[] args) {
        SpringApplication.run(HikvisionApplication.class, args);
    }
}