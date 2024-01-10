package com.steccn.pms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数采数据DTO
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@Data
public class DataCollectDTO {
    private LocalDateTime timestamp;
    private BigDecimal value;
}
