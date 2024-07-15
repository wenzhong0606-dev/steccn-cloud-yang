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
 * 玻璃板宽
 * @TableName pull_produ_par_detail2
 */
@TableName(value ="pull_produ_par_detail2")
@Data
public class PullProduParDetail2 implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private LocalDateTime dt;

    /**
     * 
     */
    private BigDecimal idValue;

    /**
     * 
     */
    private Integer productLine;

    /**
     * 
     */
    private Long partitionDatehour;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}