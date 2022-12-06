package com.steccn.data.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName ep_energy_price_plan
 */
@TableName(value ="ep_energy_price_plan")
@Data
public class EpEnergyPricePlan implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private LocalDate validStart;

    /**
     * 
     */
    private LocalDate validEnd;

    /**
     * 
     */
    private Float peakPrice;

    /**
     * 
     */
    private Float peacePrice;

    /**
     * 
     */
    private Float valleyPrice;

    /**
     * 
     */
    private Long unit;

    /**
     * 
     */
    private Long createBy;

    /**
     * 
     */
    private String createName;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private Long updateBy;

    /**
     * 
     */
    private String updateName;

    /**
     * 
     */
    private LocalDateTime updateTime;

    /**
     * 
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}