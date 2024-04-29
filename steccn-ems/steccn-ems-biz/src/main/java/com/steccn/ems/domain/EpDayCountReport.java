package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName ep_day_count_report
 */
@TableName(value ="ep_day_count_report")
@Data
public class EpDayCountReport implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 能源类型
     */
    private String energyType;

    /**
     * 单位
     */
    private String energyUnit;

    /**
     * 部门名
     */
    private String departmentName;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 能耗值
     */
    private BigDecimal consumeValue;

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