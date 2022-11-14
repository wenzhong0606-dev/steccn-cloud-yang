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
 * @author 王志雄
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QtLineChartBarDTO<T> implements Serializable {
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
     * 第二层数据
     */
    private List<QtSeriesDataDTO> seriesDataVO;
}
