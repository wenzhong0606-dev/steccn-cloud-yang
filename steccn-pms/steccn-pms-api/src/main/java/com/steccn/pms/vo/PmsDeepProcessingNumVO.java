package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PmsDeepProcessingNumVO {
    /**
     * 领用总数
     */
    private BigDecimal receiveQuantity;
    /**
     * 耗用总数
     */
    private BigDecimal consumptionQuantity;
    /**
     * 耗用总数
     */
    private BigDecimal useQuantity;
}
