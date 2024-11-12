package com.steccn.dp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * value类型DTO
 *
 * @author AndrewLuo
 * @date 2024/09/23
 */
@Data
public class DpValueDTO {
    private String tagId;
    private String nodeId;
    private String kafkaTopic;
    private String opcPath;
    private Long serverTimeMs;
    private String serverTime;
    private BigDecimal value;
    private String valueAsString;
    private String statusCode;
    private String valueType;
}
