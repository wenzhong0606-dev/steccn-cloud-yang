package com.steccn.ems.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EpEnergyPriceVO implements Serializable {
    /**
     * 价格方案id
     */
    private Long id;

    /**
     * 有效时间-开始
     */
    private LocalDate validStart;

    /**
     * 有效时间-结束
     */
    private LocalDate validEnd;

    /**
     * 峰时段单价
     */
    private BigDecimal peakPrice;

    /**
     * 平时段单价
     */
    private BigDecimal peacePrice;

    /**
     * 谷时段单价
     */
    private BigDecimal valleyPrice;

    /**
     * 能源价格方案详情单元格
     */
    private List<Integer> pricePlanType;

    private static final long serialVersionUID = 1L;
}
