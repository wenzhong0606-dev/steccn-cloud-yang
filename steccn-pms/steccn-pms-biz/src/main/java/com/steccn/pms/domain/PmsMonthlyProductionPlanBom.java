package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 生产工单-物料清单信息
 * @TableName pms_monthly_production_plan_bom
 */
@TableName(value ="pms_monthly_production_plan_bom")
@Data
public class PmsMonthlyProductionPlanBom implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 组件id
     */
    private Long moduleId;

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

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}