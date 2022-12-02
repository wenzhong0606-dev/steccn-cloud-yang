package com.steccn.ems.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/1 15:44
 */
@Data
public class EpMeterReadAddDTO {
    private Integer meterId;
    private Long partitionId;
    private BigDecimal readValue;
    private LocalDate readDate;
    private Integer adminFlag;
}
