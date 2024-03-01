package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 冷端各线测点数据
 * @TableName dp_cold_end_checkpoint_value
 */
@TableName(value ="dp_cold_end_checkpoint_value")
@Data
public class DpColdEndCheckpointValue implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 测点code
     */
    private String checkPointCode;

    /**
     * 测点名称
     */
    private String checkPointName;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateDate;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 备注信息
     */
    private String remarks;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}