package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 任务执行表
 *
 * @TableName ep_task_execute
 */
@TableName(value = "ep_task_execute")
@Data
public class EpTaskExecute implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务类型
     */
    private Integer type;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 任务条数
     */
    private Integer taskParam;

    /**
     * 任务条数
     */
    private Integer taskCount;

    /**
     * 任务状态
     * @see com.steccn.ems.constant.EpManagerConstant.TaskFlagEnum
     */
    private Integer taskFlag;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}