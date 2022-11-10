package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/10 15:37
 */
@Data
public class PmsPullForceClassQuantity {
    private Long id;

    private Long classId;

    private BigDecimal quantity;
}
