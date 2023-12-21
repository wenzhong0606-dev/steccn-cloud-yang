package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 深加工报表行对象
 * @date 2023/12/21
 */
@Data
public class SjgCountReportRowVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 类别 - 工序名 | 电
     */
    private String categoryName;

    /**
     * 产线
     */
    private String lineName;

    /**
     * 本区间数值
     */
    private BigDecimal currentValue;

    /**
     * 本区间数值比率
     */
    private BigDecimal currentValueRate;


    /**
     * 前区间数值
     */
    private BigDecimal preValue;

    /**
     * 前区间数值比率
     */
    private BigDecimal preValueRate;

    /**
     * 差值-产量
     */
    private String valueDiff;

    /**
     * 差值-比率
     */
    private String valueDiffRate;

    /**
     * 本区间合计
     */
    private String currentValueSum;

    /**
     * 本区间合计-比率
     */
    private String currentValueSumRate;

    /**
     * 本区间合计
     */
    private String preValueSum;

    /**
     * 本区间合计-比率
     */
    private String preValueSumRate;

    /**
     * 差值-数值
     */
    private String valueDiffSum;

    /**
     * 差值-比率
     */
    private String valueDiffSumRate;
}
