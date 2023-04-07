package com.steccn.data.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author yequan
 * @desc
 * @date 2023/3/14
 */
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Configuration
public class MyConfig {


//    CREATE TABLE IF NOT EXISTS
}
