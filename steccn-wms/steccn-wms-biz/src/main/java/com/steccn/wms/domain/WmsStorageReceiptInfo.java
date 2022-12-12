package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @TableName wms_storage_receipt_info
 */
@TableName(value = "wms_storage_receipt_info")
@Data
public class WmsStorageReceiptInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 入库单id
     */
    private Long storageReceiptId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;


    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 货架名称
     */
    private String shelfName;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private Long unit;

    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 关联箱号
     */
    private String associatedNo;

    /**
     * 有效期截至时间
     */
    private LocalDate validTime;

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