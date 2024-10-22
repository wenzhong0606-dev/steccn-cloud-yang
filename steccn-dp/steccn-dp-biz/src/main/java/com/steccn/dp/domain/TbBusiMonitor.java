package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 
 * @TableName tb_busi_monitor
 */
@TableName(value ="tb_busi_monitor")
@Data
public class TbBusiMonitor implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 类别
     */
    private Integer category;

    /**
     * 告警code
     */
    private String alarmCode;

    /**
     * 0-停用 1-启用
     */
    private Integer state;

    /**
     * 上限值
     */
    private BigDecimal upValue;

    /**
     * 下限值
     */
    private BigDecimal lowValue;

    /**
     * 监控范围
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> monitorRange;

    /**
     * 通知人员
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Integer> notifyMan;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}