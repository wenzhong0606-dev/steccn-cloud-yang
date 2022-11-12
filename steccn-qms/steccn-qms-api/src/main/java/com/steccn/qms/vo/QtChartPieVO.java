package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 饼状图VO
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartPieVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 饼状图VO
     */
    private List<QtChartPieValueVO<T>> data;
}
