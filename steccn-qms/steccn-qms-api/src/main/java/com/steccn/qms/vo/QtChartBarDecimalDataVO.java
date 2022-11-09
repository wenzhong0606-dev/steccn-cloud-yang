package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 柱状图折线图VO
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartBarDecimalDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * BigDecimal数据
     */
    private List<BigDecimal> data;
}
