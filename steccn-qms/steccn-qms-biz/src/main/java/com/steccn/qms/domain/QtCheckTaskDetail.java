package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 巡检任务详细信息表
 * @TableName qt_check_task_detail
 */
@TableName(value ="qt_check_task_detail")
@Data
public class QtCheckTaskDetail implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 巡检任务id
     */
    private Long taskId;

    /**
     * 检验结果的快照json，包含id和name
     */
    private Object checkResult;

    /**
     * 是否暂存状态【0否，1是】，暂存任务是不可以提交
     */
    private Integer checkResultStatus;

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