package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 理论拉引量VO
 *
 * @author AndrewLuo
 * @date 2024/06/26
 */
@Data
public class DpTheoryPullForceDataVO {
    /**
     * 产线名称
     */
    private String productLineName;
    /**
     * 平均拉引速度
     */
    private BigDecimal avgSpeed;
    /**
     * 玻璃板宽
     */
    private BigDecimal glassWidth;
    /**
     * 玻璃厚度
     */
    private BigDecimal glassThick;
    /**
     * 理论拉引量
     */
    private BigDecimal theoryPullForce;
    /**
     * 实际拉引量
     */
    private BigDecimal actualPullForce;
}
