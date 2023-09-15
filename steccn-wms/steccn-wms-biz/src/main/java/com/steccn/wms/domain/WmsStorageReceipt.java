package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @TableName wms_storage_receipt
 */
@TableName(value = "wms_storage_receipt")
@Data
public class WmsStorageReceipt implements Serializable {
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

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 单据来源类型 0生产 1采购系统
     */
    private Integer sourceType;


    /**
     * 来源单号
     */
    private String sourceCode;

    /**
     * 金蝶收料单编码
     */
    private String erpReceiptCode;

    /**
     * 金蝶收料单id
     */
    private String erpReceiptId;
}