package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 柱状图柱子
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartBarSeriesVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 柱子名字
     */
    private String name;

    /**
     * 数据
     */
    private List<T> data;

    /**
     * 堆叠的盏
     */
    private String stack;
}
