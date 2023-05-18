package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 远传仪表读数数据
 *
 * @TableName ep_meter_read_distant
 */
@TableName(value = "ep_meter_read_distant")
@Data
public class EpMeterReadDistant implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 仪表id
     */
    private Integer meterId;

    /**
     * 远传表映射编码
     */
    private String distantMapCode;

    /**
     * 抄表日期
     */
    private LocalDate readDate;

    /**
     * 班次id 0早班 1晚班
     */
    private Integer classId;

    /**
     * 厚度
     */
    private BigDecimal thick;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 开始时间
     */
    private LocalDateTime start;

    /**
     * 结束时间
     */
    private LocalDateTime end;

    /**
     * 抄表值
     */
    private BigDecimal readValue;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}