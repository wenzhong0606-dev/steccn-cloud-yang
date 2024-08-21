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
 * @TableName opc_ua_data_count
 */
@TableName(value ="opc_ua_data_count")
@Data
public class OpcUaDataCount implements Serializable {
    /**
     * 
     */
    private String path;

    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private BigDecimal idValue;

    /**
     * 
     */
    private LocalDateTime dt;

    /**
     * 
     */
    private String idCategory;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}