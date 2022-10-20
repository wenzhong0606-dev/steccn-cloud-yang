package com.steccn.common.properties;

import com.steccn.common.constant.LogServiceEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author yequan
 * @desc 日志配置
 */
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(LogProperties.PREFIX)
@Slf4j
public class LogProperties {

	public Boolean enable = true;
    /**
     * 前缀
     */
    public static final String PREFIX = "steccn.log";

    /**
     * 记录日志类型
     */
    private LogServiceEnum logType = LogServiceEnum.DB;

    public void setEnable(Boolean enable) {
        log.info("enable -> {}",enable);
        this.enable = enable;
    }
}
