package com.steccn.pms.vo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PmsMonthlyProductionPlanCraftVO {

    private Long id;

    /**
     * 工序编码
     */
    private String code;

    /**
     * 工序名称
     */
    private String name;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 工序类型id
     */
    private Long typeId;

    private String unit;

    private String type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 标准数量
     */
    private BigDecimal standardQuantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 是否报工：（ 0：否 1：是）
     */
    private Integer reportWorkFlag;

    /**
     * 是否报检：（ 0：否 1：是）
     */
    private Integer reportCheckFlag;

    /**
     * 是否进站：（ 0：否 1：是）
     */
    private Integer inStationFlag;

    /**
     * 是否出站：（ 0：否 1：是）
     */
    private Integer outStationFlag;

    /**
     * 生产主计划id
     */
    private Long monthlyProductionPlanId;

    /**
     * 工艺路线工序id
     */
    private Long craftRouteProcessId;
}
