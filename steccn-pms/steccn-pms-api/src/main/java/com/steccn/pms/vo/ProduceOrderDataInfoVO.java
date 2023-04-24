package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduceOrderDataInfoVO {

    /**
     * 产线名称
     */
    private String productionLineName;

    /**
     * 产线唯一标识
     */
    private Integer productionLineId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料唯一标识
     */
    private Long materialId;

    /**
     * 物料规格
     */
    private String specification;


    /**
     * 计划数量
     */
    private BigDecimal planQuantity;

    /**
     * 当前产量
     */
    private BigDecimal currentProduction;


    /**
     * 单位id
     */
    private Long unitId;


    /**
     * 单位名称
     */
    private String unitName;
}
