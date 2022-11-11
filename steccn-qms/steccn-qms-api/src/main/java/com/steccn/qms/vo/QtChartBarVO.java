package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 柱状图VO
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartBarVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * X轴数据
     */
    private List<String> xAis;

    /**
     * 折线图
     */
    private QtChartBarDecimalDataVO markLine;

    /**
     * 柱状图
     */
    private List<QtChartBarSeriesVO> series;
}
