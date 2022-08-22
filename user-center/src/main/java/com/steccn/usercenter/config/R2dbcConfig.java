package com.steccn.usercenter.config;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Configuration
public class R2dbcConfig {

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        MySqlConnectionConfiguration configuration =
//                MySqlConnectionConfiguration.builder()
//                        .host(host)
//                        .port(port)
//                        .database(database)
//                        .schema(schema)
//                        .username(username)
//                        .password(password)
//                        .build();
//
//        ConnectionFactory fg = new PostgresqlConnectionFactory(configuration);
//        ConnectionPoolConfiguration connectionPoolConfiguration =
//                ConnectionPoolConfiguration.builder(fg)
//                        .maxIdleTime(Duration.ofMillis(maxIdleTime)).acquireRetry(acquireRetry)
//                        .initialSize(initialSize).maxSize(maxSize).build();
//        return new ConnectionPool(connectionPoolConfiguration);
//    }
}
