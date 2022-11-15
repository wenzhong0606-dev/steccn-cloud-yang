package com.steccn.pms.vo;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/11/9 18:32
 */
@Data
public class PmsReportWorkGroupVO {

    private LocalDateTime date;

    private LocalDate groupDate;

    private BigDecimal quantity;

    private Long classesId;

    /**
     * 车间id
     */
    private Integer workshopId;

    /**
     * 产线id
     */
    private Integer productLineId;

}
