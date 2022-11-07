package com.steccn.qms.vo;


import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
public class QtChartAcceptabilityVO {
    /**
     * 横坐标
     */
    private List<T> xAxis;
    /**
     * 纵坐标
     */
    private List<T> yAxis;


    /**
     * 合格率
     */
    private String acceptability;


}
