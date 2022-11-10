package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 部门分析驾驶舱返回DTO
 */
@Data
@ApiModel("部门分析驾驶舱返回DTO")
public class QtDefectChartDTO {
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
    private String value;

    /**
     * 日环比%
     */
    private String dayValue;

    /**
     * 周同比%
     */
    private String weekValue;

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
    private List<String> xData;

    /**
     * 纵坐标，记录一周每天的数值
     */
    private List<Integer> yData;

    /**
     * 目标值
     */
    private Integer targetValue;

    /**
     * 每个卡片提示的当天合格率或缺陷总数提示信息
     */
    private String tooltip;


}
