package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName pms_monthly_production_plan
 */
@TableName(value ="pms_monthly_production_plan")
@Data
public class PmsMonthlyProductionPlan implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户
     */
    private String client;

    private Integer clientId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 月度
     */
    private String monthly;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 计划数量
     */
    private BigDecimal plannedQuantity;

    /**
     * 已转数量
     */
    private BigDecimal transferredQuantity;

    /**
     * 完成数量
     */
    private BigDecimal completionQuantity;

    /**
     * 工艺版本
     */
    private String processVersion;

    private Long craftId;

    /**
     * 最小包装数量
     */
    private BigDecimal minimumPackingQuantity;


    private Long unitId;

    private Long minimumPackingQuantityUnit;
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