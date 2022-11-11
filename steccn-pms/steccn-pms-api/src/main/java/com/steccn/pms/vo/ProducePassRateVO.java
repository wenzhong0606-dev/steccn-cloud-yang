package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/9 16:43
 */
@Data
public class ProducePassRateVO {

    private LocalDate date;

    /**
     * 班次id
     */
    private Long classesId;

    /**
     * 车间id
     */
    private Integer workshopId;

    /**
     * 产线id
     */
    private Integer productLineId;

    private BigDecimal passRate;
}
