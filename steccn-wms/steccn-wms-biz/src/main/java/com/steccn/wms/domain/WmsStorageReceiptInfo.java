package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName wms_storage_receipt_info
 */
@TableName(value ="wms_storage_receipt_info")
@Data
public class WmsStorageReceiptInfo implements Serializable {
    /**
     * 主键
     */
    @TableId
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
    private LocalDateTime validTime;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}