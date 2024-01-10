package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SamplingWaterRateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检验任务主键
     */
    private Long taskId;

    /**
     * 抽样时间
     */
    private LocalDateTime samplingTime;

    /**
     * 1#仓硅砂含水率
     */
    private BigDecimal value1;

    /**
     * 2#仓硅砂含水率
     */
    private BigDecimal value2;

    /**
     * 配合料
     */
    private BigDecimal value3;
}
