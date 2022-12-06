package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 部门分析驾驶舱返回VO
 */
@Data
@ApiModel("部门分析驾驶舱返回VO")
public class QtDefectChartVO<K,V,T> {
    /**
     * 部门分析驾驶舱概况的卡片名称
     */
    private String title;

    /**
     * 部门分析驾驶舱概况的卡片id
     */
    private Integer key;

    /**
     * 部门驾驶舱概览的成品率，合格率，缺陷总数等值
     */
    private BigDecimal value;

    /**
     * 日环比%
     */
    private BigDecimal dayValue;

    /**
     * 周同比%
     */
    private BigDecimal weekValue;

    /**
     * 日环比  0 下降
     *        1 上升
     */
    private Integer dayFlag;

    /**
     * 周同比 0 下降
     *       1 上升
     */
    private Integer weekFlag;

    /**
     * 横坐标,记录星期一到日
     */
    private List<K> xData;

    /**
     * 纵坐标，记录一周每天的数值
     */
    private List<V> yData;

    /**
     * 目标值
     */
    private T targetValue;

    /**
     * 每个卡片提示的当天合格率或缺陷总数提示信息
     */
    private String tooltip;


}
