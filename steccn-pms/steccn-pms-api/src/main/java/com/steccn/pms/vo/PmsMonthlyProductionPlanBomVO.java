package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class PmsMonthlyProductionPlanBomVO {

    private Long id;

    /**
     * 组件id
     */
    private Long moduleId;

    private String name;

    private String code;

    private String processName;

    private String Unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 是否固定数量：（0：否 1：是）
     */
    private Integer fixedQuantityFlag;

    /**
     * 是否副产品：（0:否 1：是）
     */
    private Integer coproductFlag;

    /**
     * 是否反冲：（0：否  1：是）
     */
    private Integer backCalculateFlag;

    /**
     * 是否联产品：（0：否 1：是）
     */
    private Integer jointProductFlag;

    /**
     * 工序报废百分比
     */
    private Double processScrapingPercent;

    /**
     * 组件报废百分比
     */
    private Double componentScrapingPercent;

    /**
     * 月度主计划id
     */
    private Long monthlyProductionPlanId;

    /**
     * 工艺路线组件id
     */
    private Long craftRouteModuleId;

    /**
     * 是否回收
     */
    private Integer recycleFlag;

    /**
     * 是否主物料
     */
    private Integer mainMaterialFlag;

    /**
     * 来源
     */
    private Integer source;

    /**
     * 来源id
     */
    private Long sourceId;
}
