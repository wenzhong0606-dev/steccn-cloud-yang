package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

/**
 * 原料每天的值
 * @TableName raw_num
 */
@TableName(value ="raw_num")
@Data
public class RawNum implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private LocalDate day;

    /**
     * 
     */
    private BigDecimal value;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}