package com.steccn.pms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库数DTO
 *
 * @author AndrewLuo
 * @date 2023/10/10
 */
@Data
public class PmsStorageCountDTO {
    private Integer produceOrderId;
    private BigDecimal quantity;
    private LocalDateTime enterTime;
    private Long materialId;
    private Long customerId;
}
