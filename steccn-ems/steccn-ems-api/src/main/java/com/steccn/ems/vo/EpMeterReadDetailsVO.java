package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/12/1 17:06
 */
@Data
public class EpMeterReadDetailsVO {
    private Integer entityTable;
    private Long id;
    private Long meterId;
    private String meterName;
    private Integer meterType;
    private String meterTypeName;
    private BigDecimal readValue;
    private Long partitionId;
    private String partitionNode;
    private Integer status;
}
