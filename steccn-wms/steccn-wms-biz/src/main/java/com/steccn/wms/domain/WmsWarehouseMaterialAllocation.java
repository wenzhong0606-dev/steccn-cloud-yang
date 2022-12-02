package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓储物料配置
 * @TableName wms_warehouse_material_allocation
 */
@TableName(value ="wms_warehouse_material_allocation")
@Data
public class WmsWarehouseMaterialAllocation implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 安全库存
     */
    private BigDecimal safeInventory;

    /**
     * 批次送货量
     */
    private BigDecimal batchDeliveryQuantity;

    /**
     * 送货周期
     */
    private BigDecimal deliveryPeriod;

    /**
     * 送货周期单位
     */
    private Long deliveryPeriodUnit;

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

    @TableField(exist = false)
    private String deliveryPeriodUnitName;//送货周期单位
    @TableField(exist = false)
    private String materialUnitName;//物料标准单位
}