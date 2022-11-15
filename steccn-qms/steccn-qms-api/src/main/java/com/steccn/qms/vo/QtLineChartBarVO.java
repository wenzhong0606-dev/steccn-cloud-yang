package com.steccn.qms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

/**
 * 折线图VO
 * @author 王志雄
 * @param
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QtLineChartBarVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 年
     */
    private String year;

    /**
     * x轴数据
     */
    private List<T> xData;

    /**
     * 堆叠的盏,固定值
     */
    private String stack;

    /**
     * 图例
     */
    private List<String> name;

    /**
     *第二层数据
     */
    private List<QtSeriesDataVO> seriesDataVO;
}
