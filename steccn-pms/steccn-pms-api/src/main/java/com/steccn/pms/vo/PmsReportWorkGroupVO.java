package com.steccn.pms.vo;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/9 18:32
 */
@Data
public class PmsReportWorkGroupVO {

    private LocalDate date;

    private BigDecimal quantity;

    private Long groupId;

}
