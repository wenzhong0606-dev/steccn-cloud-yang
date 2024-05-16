package com.steccn.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbEventDTO {
    private HikDbEventDataDTO data;
    private String eventId;
    private String eventType;
    private Long happenTime;
    private String srcIndex;
    private String srcName;
    private String srcType;
    private Integer status;
    private BigDecimal timeout;
}
