package com.steccn.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 饼状图数据VO
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartPieValueVO<T> implements Serializable {
    /**
     * 数值
     */
    private T value;

    /**
     * 名字
     */
    private String name;

    /**
     * 占比
     */
    private BigDecimal percent;
}
