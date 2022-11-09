package com.steccn.qms.vo;

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
public class QtChartPieValueVO implements Serializable {
    /**
     * 数值
     */
    private BigDecimal value;

    /**
     * 名字
     */
    private String name;
}
