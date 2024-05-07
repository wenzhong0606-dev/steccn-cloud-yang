package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 碳排放总量集合vo
 */
@Data
public class CeCarbonSumReportListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 类别
     */
    private String categoryName;

    /**
     * 明细
     */
    private String detailName;

    /**
     * 动态列
     */
    private List<BigDecimal> valueList;

    /**
     * 合计（吨）
     */
    private BigDecimal sum;

}
