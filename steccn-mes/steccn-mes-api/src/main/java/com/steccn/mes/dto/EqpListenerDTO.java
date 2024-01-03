package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备卡夫卡监听DTO
 *
 * @author AndrewLuo
 * @date 2024/01/03
 */
@Data
public class EqpListenerDTO implements Serializable {
    private String nodeId;
    private String tagId;
    private String kafkaTopic;
    private String valueAsString;
}
