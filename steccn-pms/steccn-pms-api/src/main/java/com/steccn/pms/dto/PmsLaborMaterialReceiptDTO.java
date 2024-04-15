package com.steccn.pms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 劳保物料领用数量DTO
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborMaterialReceiptDTO {
    private BigDecimal receiptNum;
    private LocalDateTime receiptTime;
}
