package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 损耗全量数据表
 * @TableName qt_loss_data
 */
@Data
public class QtLossData implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 班次时间
     */
    private LocalDate workOrderTime;

    /**
     * 班次
     */
    private String workOrder;

    /**
     * 车间id
     */
    private Integer productWorkShopId;

    /**
     * 车间名称
     */
    private String productWorkShopName;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 产线名称
     */
    private String productLineName;

    /**
     * 损耗原因
     */
    private String reason;

    /**
     * 损耗数
     */
    private BigDecimal lossWeight;

    /**
     * 拉引量
     */
    private BigDecimal pullAmount;

    /**
     * 班组id
     */
    private Integer classGroupId;

    /**
     * 班组名称
     */
    private String classGroupName;

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