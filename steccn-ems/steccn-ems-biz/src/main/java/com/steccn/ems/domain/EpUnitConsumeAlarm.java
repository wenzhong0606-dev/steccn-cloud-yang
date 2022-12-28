package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单耗告警表
 * @TableName ep_unit_consume_alarm
 */
@TableName(value ="ep_unit_consume_alarm")
@Data
public class EpUnitConsumeAlarm implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 能源类型id
     */
    private Integer energyType;

    /**
     * 告警分区节点
     */
    private Long officeId;

    /**
     * 告警分区节点名称
     */
    private String officeName;

    /**
     * 告警时间
     */
    private LocalDateTime alarmDate;

    /**
     * 告警类别
     */
    private Integer alarmType;

    /**
     * 告警详情
     */
    private String content;

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