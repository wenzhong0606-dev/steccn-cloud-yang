package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 点检计划绑定表
 * @TableName mes_eqp_ins_plan_schedule
 */
@TableName(value ="mes_eqp_ins_plan_schedule")
@Data
public class MesEqpInsPlanSchedule implements Serializable {
    /**
     * 数据id
     */
    @TableId
    private Long id;

    /**
     * 计划id
     */
    private Long planId;

    /**
     * 执行时间
     */
    private String executeTime;

    /**
     * 执行时长
     */
    private Long executePeriod;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 
     */
    private Integer day;

    /**
     * 
     */
    private Integer week;

    /**
     * 
     */
    private Integer month;

    /**
     * 计划类型(0班组点检，1日点检，2周点检，3月点检，4季度点检)
     */
    private String planType;

    /**
     * 执行日期
     */
    private Integer executeDay;

    /**
     * 执行周
     */
    private Integer executeWeek;

    /**
     * 执行月
     */
    private Integer executeMonth;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 最后一次执行时间
     */
    private Date lastExecuteTime;

    /**
     * 启用停用(0停用，1启用)
     */
    private String state;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 班次id
     */
    private Long scheduleId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}