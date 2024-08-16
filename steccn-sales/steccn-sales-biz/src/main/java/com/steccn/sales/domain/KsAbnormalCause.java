package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 异常原因
 * @TableName ks_abnormal_cause
 */
@TableName(value ="ks_abnormal_cause")
@Data
public class KsAbnormalCause implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    /**
     * Created by
     */
    private Integer createUid;

    /**
     * Last Updated by
     */
    private Integer writeUid;

    /**
     * 原因名称
     */
    private String name;

    /**
     * Created on
     */
    private LocalDateTime createDate;

    /**
     * Last Updated on
     */
    private LocalDateTime writeDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}