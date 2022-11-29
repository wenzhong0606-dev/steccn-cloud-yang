package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 盘点单明细
 * @TableName pms_check_order_info
 */
@TableName(value ="pms_check_order_info")
@Data
public class PmsCheckOrderInfo implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 仓库
     */
    private Integer warehouse;

    /**
     * 货架
     */
    private Integer shelf;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 单位id
     */
    private Long unit;

    /**
     * 结果
     */
    private BigDecimal result;

    /**
     * 差额
     */
    private BigDecimal difference;

    /**
     * 盘点人id
     */
    private Long checkBy;

    /**
     * 盘点人名称
     */
    private String checkName;

    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 状态（0：正常 1：异常）
     */
    private Integer status;

    /**
     * 盘点单id
     */
    private Long checkOrderId;

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