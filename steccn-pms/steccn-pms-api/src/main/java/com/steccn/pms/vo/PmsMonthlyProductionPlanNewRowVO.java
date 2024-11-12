package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PmsMonthlyProductionPlanNewRowVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 客户主键
     */
    private Integer clientId;

    /**
     * 客户名称
     */
    private String client;

    /**
     * 指标名称
     */
    private String indicatorName;

    /**
     * 合计
     */
    private BigDecimal sum;

    /**
     * 动态列值
     */
    private List<BigDecimal> value;

}
