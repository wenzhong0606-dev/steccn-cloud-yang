package com.steccn.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班次时间
 *
 * @author AndrewLuo
 * @date 2022/12/05
 */
@Data
public class QtTaskClassCalculate implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * 班次类型 0早班 1晚班
     */
    private Integer classType;

    /**
     * 班次日期
     */
    private LocalDateTime classDate;
}
