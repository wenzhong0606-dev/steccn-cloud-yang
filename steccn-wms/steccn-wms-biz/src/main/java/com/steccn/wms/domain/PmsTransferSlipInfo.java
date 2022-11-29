package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 调拨单明细
 * @TableName pms_transfer_slip_info
 */
@TableName(value ="pms_transfer_slip_info")
@Data
public class PmsTransferSlipInfo implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 调拨单id
     */
    private Long transerSlipId;

    /**
     * 来源货架
     */
    private Integer sourceShelf;

    /**
     * 目标货架
     */
    private Integer targetShlf;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private Long unit;

    /**
     * 关联单据
     */
    private String associatedNo;

    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

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