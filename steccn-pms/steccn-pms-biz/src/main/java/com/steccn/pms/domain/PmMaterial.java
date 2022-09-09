package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 物料清单-物料表
 *
 * @TableName pm_material
 */
@TableName(value = "pm_material")
@Data
public class PmMaterial implements Serializable {
    /**
     * 表ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物料清单ID
     */
    private Long bomId;


    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String code;

    /**
     * 物料名称
     */
    private String name;

    /**
     * 物料数量
     */
    private Integer quantity;

    /**
     * 单位ID
     */
    private Long unitId;

    /**
     * 是否是固定数量：1是0否
     */
    private Integer isFixedQuantity;

    /**
     * 是否是副产品：1是0否
     */
    private Integer isCopruduct;

    /**
     * 是否是联产品：1是0否
     */
    private Integer isJointProduct;

    /**
     * 是否是反冲：1是0否
     */
    private Integer isBackCalculation;

    /**
     * 工序报废百分比
     */
    private Integer processScrapingPercent;

    /**
     * 组件报废百分比
     */
    private Integer componentScrapingPercent;

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