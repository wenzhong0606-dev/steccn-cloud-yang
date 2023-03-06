package com.steccn.canal.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2023/3/2
 */
@Configuration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class MyConfig {
}
