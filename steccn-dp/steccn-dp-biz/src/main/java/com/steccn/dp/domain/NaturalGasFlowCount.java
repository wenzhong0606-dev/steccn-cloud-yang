package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 天然气每小时流量均值统计
 * @TableName natural_gas_flow_count
 */
@TableName(value ="natural_gas_flow_count")
@Data
public class NaturalGasFlowCount implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private LocalDateTime dt;

    /**
     * 
     */
    private BigDecimal idValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}