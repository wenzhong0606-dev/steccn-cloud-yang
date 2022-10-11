package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生产订单-完工入库
 *
 * @author yequan
 * @TableName pms_complete_storage
 */
@TableName(value = "pms_complete_storage")
@Data
public class PmsCompleteStorage implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 入库单编码
     */
    private String code;

    /**
     * 入库时间
     */
    private LocalDateTime inStorageTime;

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
     * 类型id
     */
    private Long typeId;

    /**
     * 入库凭证
     */
    private String inStorageVoucher;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 产线id
     */
    private Integer productLineId;

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