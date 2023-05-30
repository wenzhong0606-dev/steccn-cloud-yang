package com.steccn.hikvision.api;


import com.hikvision.artemis.sdk.config.ArtemisConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${artemis.host}")
    private String host;

    @Value("${artemis.appKey}")
    private String appKey;

    @Value("${artemis.appSecret}")
    private String appSecret;

    @Bean
    public ArtemisConfig artemisConfig() {
        ArtemisConfig config = new ArtemisConfig();
        config.setHost(host);
        config.setAppKey(appKey);
        config.setAppSecret(appSecret);
        return config;
    }
}
