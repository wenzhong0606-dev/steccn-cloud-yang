package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 调拨单明细
 * @TableName wms_transfer_slip_info
 */
@TableName(value ="wms_transfer_slip_info")
@Data
public class WmsTransferSlipInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 调拨单id
     */
    private Long transferSlipId;

    /**
     * 源库存id
     */
    private Long sourceInventoryId;

    /**
     * 来源货架
     */
    private Integer sourceShelf;

    /**
     * 来源货架名称
     */
    private String sourceShelfName;

    /**
     * 来源货架编码
     */
    private String sourceShelfCode;

    /**
     * 目标货架
     */
    private Integer targetShelf;

    /**
     * 目标货架名称
     */
    private String targetShelfName;

    /**
     * 目标货架编码
     */
    private String targetShelfCode;

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
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private Long unit;

    /**
     * 关联单据
     */
    private String associatedNo;

    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 目标箱号
     */
    private String targetCaseNo;

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