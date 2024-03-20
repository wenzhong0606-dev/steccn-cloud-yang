package com.steccn.websocket.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yequan
 * @desc kafka配置类
 * @date 2023/9/14
 */
@Component
@Data
public class KafkaConf {

    /**
     * websocket的kafka主题
     */
    @Value("${topicWs}")
    private String topicWs;

    /**
     * 端口号
     */
    @Value("${server.port}")
    private Integer serverPort;

}
