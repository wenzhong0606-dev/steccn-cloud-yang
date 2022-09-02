package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 巡检任务与复检任务关联表
 * @TableName qt_recheck_task
 */
@TableName(value ="qt_recheck_task")
@Data
public class QtRecheckTask implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Integer id;

    /**
     * 原始任务id
     */
    private Long originalId;

    /**
     * 复核任务id
     */
    private Long recheckId;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
      private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}