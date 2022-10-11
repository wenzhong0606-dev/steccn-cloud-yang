package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 生产订单
 * @TableName pms_produce_order
 */
@TableName(value ="pms_produce_order")
@Data
public class PmsProduceOrder implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 订单编码
     */
    private String code;

    /**
     * 订单类型id（字典：生产订单类型）
     */
    private Long typeId;

    /**
     * 状态（0 ：已创建  1：已下单  2：执行中 3：暂停中  4：已暂停 5：完成中 6：已完成 7：已关闭 ）
     */
    private Integer state;

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
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 产线id
     */
    private Integer productionLineId;

    /**
     * 工艺id（用做关联工艺路线信息的 工艺版本）
     */
    private Long craftId;

    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 默认货架id(完工信息)
     */
    private Integer defaultShelfId;

    /**
     * 默认仓库id（完工信息）
     */
    private Integer defaultWarehouseId;

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