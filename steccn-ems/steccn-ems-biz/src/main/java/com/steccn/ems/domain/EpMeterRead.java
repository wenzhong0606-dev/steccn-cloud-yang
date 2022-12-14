package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抄表
 *
 * @TableName ep_meter_read
 */
@TableName(value = "ep_meter_read")
@Data
public class EpMeterRead implements Serializable {
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
     * 开始时间
     */
    private LocalDateTime start;

    /**
     * 报工id
     */
    private Long reportWorkId;

    /**
     * 结束时间
     */
    private LocalDateTime end;


    /**
     * 管理员操作值
     */
    private BigDecimal adminValue;

    /**
     * 管理员操作时间
     */
    private LocalDateTime adminTime;

    /**
     * 管理员调整标识 0否 1是
     */
    private Integer adminFlag;

    /**
     * 纠正值
     */
    private BigDecimal correctValue;

    /**
     * 抄表值
     */
    private BigDecimal readValue;

    /**
     * 抄表时间
     */
    private LocalDateTime readTime;

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