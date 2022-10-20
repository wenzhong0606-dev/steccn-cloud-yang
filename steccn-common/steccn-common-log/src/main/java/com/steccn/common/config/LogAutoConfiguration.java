package com.steccn.common.config;

import com.steccn.common.aspect.SysLogAspect;
import com.steccn.common.event.SysLogListener;
import com.steccn.common.properties.LogProperties;
import com.steccn.platform.feign.RemoteLogFeignApi;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * @author yequan
 * @desc 日志自动配置
 */
@AllArgsConstructor
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnWebApplication
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = LogProperties.PREFIX, name = "enable", havingValue = "true")
    public SysLogListener sysLogListener(LogProperties logProperties, RemoteLogFeignApi remoteLogFeignApi) {
        return new SysLogListener(logProperties, remoteLogFeignApi);
    }

    @Bean
    @ConditionalOnProperty(prefix = LogProperties.PREFIX, name = "enable", havingValue = "true")
    public SysLogAspect sysLogAspect(ApplicationEventPublisher publisher) {
        return new SysLogAspect(publisher);
    }
}
