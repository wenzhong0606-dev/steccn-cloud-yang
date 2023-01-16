package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 折线图VO
 * @author 叶泉
 * @param
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QtLineChartBiBarDTO<T,Q> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * x轴数据
     */
    private List<T> xData;

    /**
     * 图例
     */
    private List<String> name;

    /**
     *第二层数据
     */
    private List<List<QtSeriesDataDTO>> seriesDataVO;

    /**
     * 其余数据
     */
    private Q other;
}
