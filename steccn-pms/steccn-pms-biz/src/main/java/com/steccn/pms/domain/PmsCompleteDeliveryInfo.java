package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 出库详情
 *
 * @author mengle
 * @TableName pms_complete_delivery_info
 */
@TableName(value = "pms_complete_delivery_info")
@Data
public class PmsCompleteDeliveryInfo implements Serializable {
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
     * 箱号
     */
    private String caseNo;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 装箱时间
     */
    private LocalDateTime packTime;

    /**
     * 装箱数量
     */
    private BigDecimal quantity;

    /**
     * 可用数量
     */
    private BigDecimal avaQuantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 出库单id
     */
    private Long completeDeliveryId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架id
     */
    private Integer shelfId;

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