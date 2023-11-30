package com.steccn.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 条形图和折线图的视图对象
 * @date 2023/11/29
 */
@Data
public class CommonBarLineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * X轴数据
     */
    private BaseValueVO<List<String>> xAxis;

    /**
     * 图例数据
     */
    private BaseValueVO<List<String>> legend;

    /**
     * 数据集合
     */
    private List<BaseValueNameVO<List<BigDecimal>>> series;

}
