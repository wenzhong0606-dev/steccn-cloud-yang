package com.steccn.pms.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @desc 基片分析报表返回视图
 */
@Data
public class JpAnalysisReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标名称
     */
    private String name;

    /**
     * 当前时间区间的值
     */
    private BigDecimal currentValue;

    /**
     * 前一个时间区间的值-环比
     */
    private BigDecimal preValue;

    /**
     * 差异
     */
    private BigDecimal diffValue;

    /**
     * 差异-百分比
     */
    private BigDecimal diffValueRate;

}
