package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 折线图VO
 *
 * @param
 * @author yequan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QtLineChartBarDTO<T,Q> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object xAxis;

    /**
     * x轴数据
     */
    private List<T> xData;

    /**
     * 图例
     */
    private List<String> legend;

    /**
     * 第二层数据
     */
    private List<QtSeriesDataDTO> seriesDataVO;

    /**
     * 其余冗余数据
     */
    private Q other;
}


