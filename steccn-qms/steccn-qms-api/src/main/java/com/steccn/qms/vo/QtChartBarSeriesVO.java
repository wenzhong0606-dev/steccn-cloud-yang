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
public class QtChartBarSeriesVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 柱状图名字
     */
    private String name;

    /**
     * 数据
     */
    private List<BigDecimal> data;
}
