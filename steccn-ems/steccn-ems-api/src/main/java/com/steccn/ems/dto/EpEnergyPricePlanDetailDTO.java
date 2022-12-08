package com.steccn.ems.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class EpEnergyPricePlanDetailDTO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 0-峰时段 1平时段 2谷时段
     */
    private Integer type;

    /**
     * 开始时间
     */
    private LocalTime start;

    /**
     * 结束时间
     */
    private LocalTime end;

    /**
     * 能源方案id
     */
    private Long planId;

    private static final long serialVersionUID = 1L;
}
