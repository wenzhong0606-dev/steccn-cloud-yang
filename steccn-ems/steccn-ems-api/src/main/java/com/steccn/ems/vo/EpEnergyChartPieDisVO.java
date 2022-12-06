package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 峰谷平分布图表
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Data
public class EpEnergyChartPieDisVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 电耗分布
     */
    private EqEnergyChartPieVO<BigDecimal> energy;

    /**
     * 电费分布
     */
    private EqEnergyChartPieVO<BigDecimal> price;
}
