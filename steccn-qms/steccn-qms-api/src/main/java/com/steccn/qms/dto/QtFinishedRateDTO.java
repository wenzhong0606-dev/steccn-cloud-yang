package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成品率报表DTO
 *
 * @author AndrewLuo
 * @date 2023/09/19
 */
@Data
public class QtFinishedRateDTO implements Serializable {
    /**
     * 0 普通行 1合计行 2总计行
     */
    private Integer type;

    /**
     * 产线
     */
    private String productLine;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 班次
     */
    private String shift;

    /**
     * 班组
     */
    private String group;

    /**
     * 长度
     */
    private BigDecimal length;

    /**
     * 宽度
     */
    private BigDecimal width;

    /**
     * 厚度
     */
    private BigDecimal thick;

    /**
     * 单片重
     */
    private BigDecimal perWeight;

    /**
     * 片
     */
    private BigDecimal slice;

    /**
     * 平方米
     */
    private BigDecimal squareMeter;

    /**
     * 称重
     */
    private BigDecimal weight;

    /**
     * 拉引量
     */
    private BigDecimal pullAmount;

    /**
     * 称重成品率
     */
    private String weightRate;

    /**
     * 换算成品率
     */
    private String convertRate;

    /**
     * 换算重
     */
    private BigDecimal convertHeight;
}
