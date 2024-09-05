package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 生产订单-工艺信息
 * @TableName pms_monthly_production_plan_craft
 */
@TableName(value ="pms_monthly_production_plan_craft")
@Data
public class PmsMonthlyProductionPlanCraft implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
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