package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日拉引量统计
 * @TableName ads_pms_pull_force
 */
@TableName(value ="ads_pms_pull_force")
@Data
public class AdsPmsPullForce implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private LocalDate pullForceDate;

    /**
     * 
     */
    private Long workshopId;

    /**
     * 
     */
    private String workshopName;

    /**
     * 
     */
    private Integer productLineId;

    /**
     * 
     */
    private String productLineName;

    /**
     * 
     */
    private Long classesId;

    /**
     * 
     */
    private String classesName;

    /**
     * 
     */
    private BigDecimal quantity;

    /**
     * 
     */
    private Long unitId;

    /**
     * 统计日期
     */
    private LocalDate dt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}