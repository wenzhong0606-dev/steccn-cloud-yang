package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 峰平谷历史对比VO
 *
 * @author AndrewLuo
 * @date 2022/12/09
 */
@Data
public class EpEnergyChartPieHisVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 电耗分布1
     */
    private EqEnergyChartPieVO<BigDecimal> energy1;

    /**
     * 电耗分布2
     */
    private EqEnergyChartPieVO<BigDecimal> energy2;

    /**
     * 电费分布
     */
    private EqEnergyChartPieVO<BigDecimal> price1;

    /**
     * 电费分布
     */
    private EqEnergyChartPieVO<BigDecimal> price2;
}
