package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 生产工单-物料清单信息
 * @TableName pms_work_order_bom
 */
@TableName(value ="pms_work_order_bom")
@Data
public class PmsWorkOrderBom implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 组件id
     */
    private Object moduleId;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 是否固定数量：（0：否 1：是）
     */
    private Integer fixedQuantityFlag;

    /**
     * 是否副产品：（0:否 1：是）
     */
    private Integer coproductFlag;

    /**
     * 是否反冲：（0：否  1：是）
     */
    private Integer backCalculateFlag;

    /**
     * 是否联产品：（0：否 1：是）
     */
    private Integer jointProductFlag;

    /**
     * 生产订单id
     */
    private Long workProduceId;

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