package com.steccn.wms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsReportDataVO {

    private  String category;

    private  String category1;

    private BigDecimal warehousing = BigDecimal.ZERO;

    private BigDecimal warehousing1 = BigDecimal.ZERO;

    private BigDecimal outbound = BigDecimal.ZERO;

    private BigDecimal outbound1 = BigDecimal.ZERO;

    private BigDecimal inventory = BigDecimal.ZERO;

    private BigDecimal inventory1 = BigDecimal.ZERO;

    private String growthRate;

}
