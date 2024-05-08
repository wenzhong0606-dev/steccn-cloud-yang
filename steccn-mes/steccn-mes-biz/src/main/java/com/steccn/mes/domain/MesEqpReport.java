package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 设备运行报表
 * @TableName mes_eqp_report
 */
@TableName(value ="mes_eqp_report")
@Data
public class MesEqpReport implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备id
     */
    private String eqpId;

    /**
     * 本次运行时长
     */
    private BigDecimal startPeriod;

    /**
     * 累计运行时长
     */
    private BigDecimal startPeriodAll;

    /**
     * 停机时长
     */
    private BigDecimal stopPeriod;

    /**
     * 最长运行时长
     */
    private BigDecimal maxStartPeriod;

    /**
     * 最短运行时长
     */
    private BigDecimal minStartPeriod;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT)
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