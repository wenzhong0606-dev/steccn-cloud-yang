package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 调拨单
 * @TableName pms_transfer_slip
 */
@TableName(value ="pms_transfer_slip")
@Data
public class PmsTransferSlip implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 调拨单编码
     */
    private String code;

    /**
     * 来源仓库
     */
    private Integer sourceWarehouse;

    /**
     * 目标仓库
     */
    private Integer targetWarehouse;

    /**
     * 类型
     */
    private Long type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String note;

    /**
     * 确认人
     */
    private Long affirmBy;

    /**
     * 确认时间
     */
    private LocalDateTime affirmTime;

    /**
     * 确认人名称
     */
    private String affirmName;

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