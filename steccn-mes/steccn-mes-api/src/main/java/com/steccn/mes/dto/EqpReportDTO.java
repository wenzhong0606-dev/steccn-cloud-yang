package com.steccn.mes.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备运行报表DTO
 *
 * @author AndrewLuo
 * @date 2024/05/08
 */
@Data
public class EqpReportDTO {
    private String EqpCode;
    /**
     * 0表示目前为关机状态 1表示开机状态
     */
    private Integer state;
    /**
     * 小时
     */
    private BigDecimal value;
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
