package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName wms_logistics_detail
 */
@TableName(value ="wms_logistics_detail")
@Data
public class WmsLogisticsDetail implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物流单id
     */
    private Long logisticsId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 发货数量
     */
    private BigDecimal shippedQuantity;

    /**
     * 到货数量
     */
    private BigDecimal aogQuantity;

    /**
     * 重量
     */
    private BigDecimal weight;

    /**
     * 体积
     */
    private BigDecimal volume;

    /**
     * 重量单位
     */
    private Long weightUnit;

    /**
     * 体积单位
     */
    private Long volumeUnit;

    /**
     * 关联单据
     */
    private String associatedNo;

    /**
     * 批次
     */
    private String bacth;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 是否拆包装（0：否  1：是）
     */
    private Integer unpackFlag;

    /**
     * 包装数量
     */
    private BigDecimal packQuantity;

    /**
     * 单位包装数量
     */
    private BigDecimal unitPackQuantity;

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