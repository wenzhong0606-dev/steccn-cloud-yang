package com.steccn.qms.vo;

import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

/**
 * 返回给前端合格率的数据
 */
public class QtAcceptabilityVO implements Serializable {
    /**
     *图表名(如:基片成品率)
     */
    private String title;

    /**
     *图表标志key第几章图表
     */
    private Integer key;

    /**
     * 合格率
     */
    private String value;

    /**
     * 日环比
     */
    private String dayValue;

    /**
     * 周同比
     */
    private String weekValue;

    /**
     * 日环比是否上升下降(0 上升 ,1 下降)
     */
    private Integer dayFlag;

    /**
     * 周同比是否上升下降(0 上升 ,1 下降)
     */
    private Integer weekFlag;

    /**
     * 横坐标数据
     */
    private List<T> xData;

    /**
     * 纵坐标数据
     */
    private List<T> yData;
}
