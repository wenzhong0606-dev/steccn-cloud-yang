package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @TableName ep_task_execute_retry
 */
@TableName(value = "ep_task_execute_retry")
@Data
public class EpTaskExecuteRetry implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务类型
     */
    private Integer type;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 失败的业务参数
     */
    private String param;

    /**
     * 失败重试次数
     */
    private Integer execCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}