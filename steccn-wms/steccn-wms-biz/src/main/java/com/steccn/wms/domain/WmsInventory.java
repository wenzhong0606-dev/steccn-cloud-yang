package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 仓储库存
 *
 * @TableName wms_inventory
 */
@TableName(value = "wms_inventory")
@Data
public class WmsInventory implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 货架名称
     */
    private String shelfName;

    /**
     * 总数
     */
    private BigDecimal totalQuantity = BigDecimal.ZERO;

    /**
     * 可用数量
     */
    private BigDecimal availableQuantity  = BigDecimal.ZERO;

    /**
     * 在途库存
     */
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    /**
     * 冻结库存
     */
    private BigDecimal freezeQuantity = BigDecimal.ZERO;

    /**
     * 锁定库存
     */
    private BigDecimal lockQuantity = BigDecimal.ZERO;

    /**
     * 质检库存
     */
    private BigDecimal detectionQuantity = BigDecimal.ZERO;

    /**
     * 单位id
     */
    private Long unit;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 批次
     */
    private String batch;

    /**
     * 打包时间
     */
    private LocalDateTime packTime;

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
}