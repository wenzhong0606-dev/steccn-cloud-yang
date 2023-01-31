package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.steccn.common.valid.InsertGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName qt_check_task_destory_info
 */
@TableName(value ="qt_check_task_destory_info")
@Data
public class QtCheckTaskDestoryInfo implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务id
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 样本数
     */
    @TableField(value = "copy")
    private Integer copy;

    /**
     * 样本编号
     */
    @TableField(value = "sample_num")
    private String sampleNum;

    /**
     * 落板明细
     */
    @TableField(value = "destory_detail")
    private String destoryDetail;

    /**
     * 落板原因
     */
    @TableField(value = "destory_reason")
    private String destoryReason;

    /**
     * 落板开始时间
     */
    @NotNull(groups = {InsertGroup.class}, message = "落板开始时间不能为空")
    @TableField(value = "destory_start_time")
    private LocalDateTime destoryStartTime;

    /**
     * 落板结束时间
     */
    @NotNull(groups = {InsertGroup.class}, message = "落板结束时间不能为空")
    @TableField(value = "destory_end_time")
    private LocalDateTime destoryEndTime;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人姓名
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 修改人id
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 修改人姓名
     */
    @TableField(value = "update_name")
    private String updateName;

    /**
     * 是否删除标识：0否 1是
     */
    @TableField(value = "del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @TableField(value = "destory_info_remark")
    private String destoryInfoRemark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}