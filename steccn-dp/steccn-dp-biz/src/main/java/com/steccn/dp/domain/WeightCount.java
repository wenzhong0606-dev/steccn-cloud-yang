package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

/**
 * 生产工序表
 * @TableName weight_count
 */
@TableName(value ="weight_count")
@Data
public class WeightCount implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 值
     */
    private BigDecimal value;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}