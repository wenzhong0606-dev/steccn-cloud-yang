package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仪表操作记录
 * @TableName ep_meter_operation_record
 */
@TableName(value ="ep_meter_operation_record")
@Data
public class EpMeterOperationRecord implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据id
     */
    private Long readId;

    /**
     * 操作节点
     */
    private Integer opNode;

    /**
     * 操作人id
     */
    private Long opBy;

    /**
     * 操作人名称
     */
    private String opName;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;

    /**
     * 操作旧值
     */
    private BigDecimal opValueNew;

    /**
     * 操作新值
     */
    private BigDecimal opValueOld;

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