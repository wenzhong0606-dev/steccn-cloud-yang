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
     * 点检项值
     */
    private BigDecimal itemValue;

    /**
     * 产线code
     */
    private String officeAuth;

    /**
     * 计划时间
     */
    private LocalDateTime dateTime;
}
