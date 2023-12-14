package com.steccn.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每天拉引量合计DTO
 *
 * @author AndrewLuo
 * @date 2023/12/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PmsPullForceDayCountDTO {
    private LocalDate pullForceDate;
    private BigDecimal quantity;
    private String pullForceDateStr;
}
