package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WmsDeliveryReceiptClientNumDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    private LocalDate classDate;

    private Long materialId;

    private String clientName;

    private BigDecimal num;
}
