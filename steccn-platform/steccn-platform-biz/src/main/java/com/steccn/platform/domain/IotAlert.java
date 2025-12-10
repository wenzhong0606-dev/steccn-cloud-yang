package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 告警表
 * @TableName iot_alert
 */
@TableName(value ="iot_alert",autoResultMap = true)
@Data
public class IotAlert implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 聚合规则
     */
    private String aggregations;

    /**
     * 下限
     */
    private BigDecimal downValue;

    /**
     * 上限
     */
    private BigDecimal upValue;

    /**
     * 时间间隔
     */
    private Integer interval;

    /**
     * 上次报警时间
     */
    private LocalDateTime alertTime;

    /**
     * 接收人
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> receiver;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 是否删除标识：0否 1是
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}