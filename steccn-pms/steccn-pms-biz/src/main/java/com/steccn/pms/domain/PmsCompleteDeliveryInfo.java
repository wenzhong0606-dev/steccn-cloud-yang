package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 出库详情
 * @TableName pms_complete_delivery_info
 */
@TableName(value ="pms_complete_delivery_info")
@Data
public class PmsCompleteDeliveryInfo implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

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
     * 状态
     */
    private Integer state;

    /**
     * 装箱数量
     */
    private BigDecimal quantity;

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
     * 生产工单id
     */
    private byte[] workOrderId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}