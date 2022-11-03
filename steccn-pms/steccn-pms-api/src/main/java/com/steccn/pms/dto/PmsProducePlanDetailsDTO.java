package com.steccn.pms.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/3 14:16
 */
@Data
public class PmsProducePlanDetailsDTO {

    private Long id;
    private String planDate;
    private BigDecimal quantity;
}
