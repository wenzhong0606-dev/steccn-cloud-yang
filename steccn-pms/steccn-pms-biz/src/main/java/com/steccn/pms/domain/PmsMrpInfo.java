package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 物料需求计划详情
 * @author yequan
 * @TableName pms_mrp_info
 */
@TableName(value ="pms_mrp_info")
@Data
public class PmsMrpInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 日期
     */
    private LocalDate mrpDate;

    /**
     * mrp类型id
     */
    private Long mrpTypeId;

    /**
     * mrp元素
     */
    private String mrpElement;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 可用库存数量
     */
    private BigDecimal availableQuantity;

    /**
     * 物料需求计划id
     */
    private Long materialId;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 状态
     */
    private Integer status;

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