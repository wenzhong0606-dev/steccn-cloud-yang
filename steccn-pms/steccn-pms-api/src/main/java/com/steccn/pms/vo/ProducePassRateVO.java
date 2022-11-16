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
     * 班次名字
     */
    private String classesName;

    /**
     * 车间id
     */
    private Integer workshopId;

    /**
     * 车间名字
     */
    private String workshopName;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 产线名字
     */
    private String productLineName;

    private BigDecimal passRate;

    /**
     * 日期类字符串
     */
    private String dateString;

    /**
     * 成品率字符串
     */
    private String passRateString;
}
