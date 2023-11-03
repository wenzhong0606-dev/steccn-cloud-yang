package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 出库单
 * @TableName wms_delivery_receipt
 */
@TableName(value ="wms_delivery_receipt")
@Data
public class WmsDeliveryReceipt implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private Long type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 确认时间
     */
    private LocalDateTime affirmTime;

    /**
     * 确认人
     */
    private String affirmName;

    /**
     * 备注
     */
    private String note;

    /**
     * 关联sales 物流单号
     */
    private String salesOrder;

    /**
     * 关联的主键-报工id
     */
    private Long linkedId;

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