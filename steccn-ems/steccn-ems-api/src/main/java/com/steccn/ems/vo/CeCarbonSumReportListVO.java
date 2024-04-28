package com.steccn.ems.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 碳排放总量集合vo
 */
@Data
public class CeCarbonSumReportListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类别
     */
    private String categoryName;

    /**
     * 明细
     */
    private String detailName;

    /**
     * 合计（吨）
     */
    private BigDecimal sum;

}
