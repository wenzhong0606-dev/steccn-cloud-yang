package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 生产订单-出库
 * @TableName pms_complete_delivery
 */
@TableName(value ="pms_complete_delivery")
@Data
public class PmsCompleteDelivery implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 出库单编码
     */
    private String code;

    /**
     * 出库时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 类型信息
     */
    private Long typeId;

    /**
     * 出库凭证
     */
    private String outStorageVoucher;

    /**
     * 报工id
     */
    private Long reportWorkId;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 工序id
     */
    private Long processId;

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