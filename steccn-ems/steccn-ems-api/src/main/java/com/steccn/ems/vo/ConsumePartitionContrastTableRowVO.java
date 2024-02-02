package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @desc 能耗分区对比最值，平均值，统计值行对象
 */
@Data
public class ConsumePartitionContrastTableRowVO implements Serializable {

    /**
     * 能耗节点
     */
    private String partitionName;

    /**
     * 厚度
     */
    private String thickName;

    /**
     * 平均值
     */
    private BigDecimal avgValue;

    /**
     * 平均值STR
     */
    private String avgValueStr;

    /**
     * 累计值
     */
    private BigDecimal sumValue;

    /**
     * 累计值str
     */
    private String sumValueStr;

    /**
     * 最大值
     */
    private BigDecimal maxValue;

    /**
     * 最大值str
     */
    private String maxValueStr;

    /**
     * 最大值发生时间
     */
    private String maxValueOccur;

    /**
     * 最小值
     */
    private BigDecimal minValue;

    /**
     * 最小值字符串
     */
    private String minValueStr;

    /**
     * 最小值发生时间
     */
    private String minValueOccur;

    /**
     * 值集合
     */
    private List<BigDecimal> valueList;

}
