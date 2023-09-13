package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 点检任务表
 * @TableName mes_eqp_ins_task
 */
@TableName(value ="mes_eqp_ins_task")
@Data
public class MesEqpInsTask implements Serializable {
    /**
     * 数据id
     */
    @TableId
    private Long id;

    /**
     * 名字
     */
    private String name;

    /**
     * 执行人
     */
    private String asignee;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private String taskType;

    /**
     * 计划时长
     */
    private Double planPeriod;

    /**
     * 计划时长单位
     */
    private String planPeriodUnit;

    /**
     * 计划时间
     */
    private LocalDateTime planTime;

    /**
     * 状态（0正常 1删除 2停用）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 完成时间
     */
    private LocalDateTime finishedTime;

    /**
     * 记录Code
     */
    private String recordCode;

    /**
     * 签收时间，等于实际开始时间
     */
    private LocalDateTime signTime;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 权限字段
     */
    private String officeCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}