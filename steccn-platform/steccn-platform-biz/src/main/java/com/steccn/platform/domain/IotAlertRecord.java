package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 告警记录表
 * @TableName iot_alert_record
 */
@TableName(value ="iot_alert_record",autoResultMap = true)
@Data
public class IotAlertRecord implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private Integer alertId;

    /**
     * 报警名称
     */
    private String alertName;

    /**
     * 内容
     */
    private String content;

    /**
     * 报警人名称
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> receiver;

    /**
     * 报警时间
     */
    private LocalDateTime alertTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}