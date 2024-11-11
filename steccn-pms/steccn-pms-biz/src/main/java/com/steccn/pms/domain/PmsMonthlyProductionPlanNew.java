package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @TableName pms_monthly_production_plan_new
 */
@TableName(value = "pms_monthly_production_plan_new")
@Data
public class PmsMonthlyProductionPlanNew implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 客户id
     */
    private Integer clientId;

    /**
     * 客户
     */
    private String client;

    /**
     * 日期
     */
    private LocalDate planDate;

    /**
     * 状态-是否下达
     */
    private Integer state;

    /**
     * 计划数量
     */
    private BigDecimal planQuantity;

    /**
     * 已转数量
     */
    private Long linkId;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}