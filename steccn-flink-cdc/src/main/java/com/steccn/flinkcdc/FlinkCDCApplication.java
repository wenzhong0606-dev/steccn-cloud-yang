package com.steccn.flinkcdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author AndrewLuo
 * @desc flinkCDC工具
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.steccn")
public class FlinkCDCApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlinkCDCApplication.class, args);
    }
}
