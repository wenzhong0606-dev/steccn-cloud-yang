package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 配方表
 * @TableName pms_recipe
 */
@TableName(value ="pms_recipe")
@Data
public class PmsRecipe implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 配方版本
     */
    private String version;

    /**
     * 熟料占比
     */
    private BigDecimal clinkerRate;

    /**
     * 熟料用量
     */
    private BigDecimal clinkerQuantity;

    /**
     * 数量单位
     */
    private Long clinkerUnit;

    /**
     * 配合料含水率
     */
    private BigDecimal batchWaterRate;

    /**
     * 加水量
     */
    private BigDecimal batchWaterQuantity;

    /**
     * 加水量单位
     */
    private Long batchWaterUnit;

    /**
     * 上偏差
     */
    private BigDecimal batchWaterUpper;

    /**
     * 下偏差
     */
    private BigDecimal batchWaterLower;

    /**
     * 备注
     */
    private String note;

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