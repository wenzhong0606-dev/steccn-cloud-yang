package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
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
     * 数量单位
     */
    private Long unitId;

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
    private Long classesId;

    /**
     * 班组id
     */
    private Long classGroupId;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 箱状态（0:已完工 1:待入库 2：已入库）
     */
    private Integer caseState;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架Id
     */
    private Integer shelfId;


    /**
     * 是否已打印
     */
    private Integer printFlag;

    /**
     * 打印次数
     */
    private Integer printCount;


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