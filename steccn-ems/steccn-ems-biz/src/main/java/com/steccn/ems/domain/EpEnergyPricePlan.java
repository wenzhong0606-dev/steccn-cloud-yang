package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 能源价格方案
 * @TableName ep_energy_price_plan
 */
@TableName(value ="ep_energy_price_plan")
@Data
public class EpEnergyPricePlan implements Serializable {
    /**
     * 价格方案id
     */
    @TableId
    private Long id;

    /**
     * 价格方案名称
     */
    private String name;

    /**
     * 有效时间-开始
     */
    private LocalDate validStart;

    /**
     * 有效时间-结束
     */
    private LocalDate validEnd;

    /**
     * 峰时段单价
     */
    private BigDecimal peakPrice;

    /**
     * 平时段单价
     */
    private BigDecimal peacePrice;

    /**
     * 谷时段单价
     */
    private BigDecimal valleyPrice;

    /**
     * 单位
     */
    private Long unit;

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