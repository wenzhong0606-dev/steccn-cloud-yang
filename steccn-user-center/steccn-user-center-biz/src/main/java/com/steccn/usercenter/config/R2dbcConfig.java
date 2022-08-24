package com.steccn.usercenter.config;

import org.springframework.context.annotation.Configuration;

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
