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

    private String groupName;

    private BigDecimal passRate;
}
