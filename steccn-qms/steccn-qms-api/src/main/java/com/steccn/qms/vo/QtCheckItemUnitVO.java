package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 根据选定的检验项id返回标准值、上下限以及单位
 */
@Data
public class QtCheckItemUnitVO implements Serializable {
    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上偏差
     */
    private BigDecimal upperValue;

    /**
     * 下偏差
     */
    private BigDecimal lowerValue;

    /**
     * 计量单位
     */
    private String measureUnitName;
}
