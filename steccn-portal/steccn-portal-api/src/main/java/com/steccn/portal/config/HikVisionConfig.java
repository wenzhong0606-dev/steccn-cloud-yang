package com.steccn.portal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 海康威视配置
 *
 * @author AndrewLuo
 * @date 2022/11/22
 */
@Component
@Data
public class HikVisionConfig {
    /**
     * 平台host地址
     */
    @Value("${hik.host}")
    private String HIK_HOST;

    /**
     * 秘钥appKey
     */
    @Value("${hik.appKey}")
    private String HIK_APP_KEY;

    /**
     * 秘钥appSecret
     */
    @Value("${hik.appSecret}")
    private String HIK_APP_SECRET;

    @Value("${hik.path}")
    private String PATH;
}
