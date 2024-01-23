package com.steccn.dp.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 开关VO
 * @author AndrewLuo
 * @date 2024/01/17
 */
@Data
public class DpOnOffCountVO {

    /**
     * 1 2 3 4 代表8条线
     */
    private String productLineNum;

    /**
     * 产线id
     */
    private Integer productLineId;
    /**
     * 班次日期
     */
    private LocalDate classDate;
    /**
     * 开始时间
     */
    private LocalTime start;
    /**
     * 结束时间
     */
    private LocalTime end;
    /**
     * 玻璃数
     */
    private Long num;
}
