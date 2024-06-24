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
 * 
 * @TableName pull_produ_par_count
 */
@TableName(value ="pull_produ_par_count")
@Data
public class PullProduParCount implements Serializable {
    /**
     * 对应code
     */
    private String id;

    /**
     * 开始时间
     */
    private LocalDateTime dt1;

    /**
     * 结束时间
     */
    private LocalDateTime dt2;

    /**
     * 数值
     */
    private BigDecimal idValue;

    /**
     * 产线
     */
    private Integer productLine;

    /**
     * 时长值
     */
    private Long partitionDatehour;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}