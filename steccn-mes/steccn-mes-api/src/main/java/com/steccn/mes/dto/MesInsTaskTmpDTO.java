package com.steccn.mes.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 温度点检项DTO
 *
 * @author AndrewLuo
 * @date 2023/11/13
 */
@Data
public class MesInsTaskTmpDTO {
    /**
     * 点检项id
     */
    private Long insItemId;

    /**
     * 点检项名称
     */
    private String insItemName;

    /**
     * 点检项值
     */
    private BigDecimal value;

    /**
     * 产线code
     */
    private String officeAuth;
    /**
     * 产线名称
     */
    private String productLineName;

    /**
     * 日期时间
     */
    private LocalDateTime dateTime;

    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 班次
     */
    private String classOrder;
    /**
     * 班组
     */
    private String classGroupName;
    /**
     * 时间
     */
    private LocalTime time;

    private LocalDateTime planTime;
}
