package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 条码打印
 *
 * @TableName pms_barcode_print
 */
@TableName(value = "pms_barcode_print")
@Data
public class PmsBarcodePrint implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 产线id
     */
    private Integer productionLine;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 打印条数
     */
    private Integer printStrip;

    /**
     * 打印张数
     */
    private Integer printNumber;

    /**
     * 班次id
     */
    private Integer classesId;

    /**
     * 班组id
     */
    private Integer classGroupId;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

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