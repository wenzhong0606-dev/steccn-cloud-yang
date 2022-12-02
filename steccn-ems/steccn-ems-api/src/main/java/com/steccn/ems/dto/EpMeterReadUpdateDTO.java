package com.steccn.ems.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/2 16:22
 */
@Data
public class EpMeterReadUpdateDTO {
    /**
     * 仪表id
     */
    private Integer meterId;
    private LocalDate readDate;
    private BigDecimal readValue;
    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;

    private Long partitionId;

    private Integer adminFlag;
}
