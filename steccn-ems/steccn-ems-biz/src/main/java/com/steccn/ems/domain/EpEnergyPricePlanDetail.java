package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 能源价格方案明细
 * @TableName ep_energy_price_plan_detail
 */
@TableName(value ="ep_energy_price_plan_detail")
@Data
public class EpEnergyPricePlanDetail implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 0-峰时段 1平时段 2谷时段
     */
    private Integer type;

    /**
     * 开始时间
     */
    private BigDecimal start;

    /**
     * 结束时间
     */
    private BigDecimal end;

    /**
     * 能源方案id
     */
    private Long planId;

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