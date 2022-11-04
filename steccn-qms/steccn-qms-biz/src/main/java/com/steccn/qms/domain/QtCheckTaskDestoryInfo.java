package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private Object id;

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
    @TableField(value = "destory_start_time")
    private LocalDateTime destoryStartTime;

    /**
     * 落板结束时间
     */
    @TableField(value = "destory_end_time")
    private LocalDateTime destoryEndTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建人姓名
     */
    @TableField(value = "create_name")
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
    private Integer delFlag;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}