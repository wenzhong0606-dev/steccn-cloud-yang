package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班次类
 *
 * @author AndrewLuo
 * @date 2022/11/15
 */
@Data
public class QtTaskClassCalculate implements Serializable {
    /**
     * 班次类型 0早班 1晚班
     */
    private Integer classType;

    /**
     * 班次日期
     */
    private LocalDateTime classDate;
}
