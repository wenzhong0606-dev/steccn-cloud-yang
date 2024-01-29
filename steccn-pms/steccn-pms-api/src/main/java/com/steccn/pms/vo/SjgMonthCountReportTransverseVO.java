package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @desc 深加工月度统计报表 - 横向
 */
@Data
public class SjgMonthCountReportTransverseVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 产线 || 车间
     */
    private String officeName;

    /**
     * 类别
     */
    private String categoryName;

    /**
     * 领用数
     */
    private BigDecimal useCount;

    /**
     * 入库数
     */
    private BigDecimal inStorageCount;

    /**
     * 成品率
     */
    private BigDecimal yieldRate;

}
