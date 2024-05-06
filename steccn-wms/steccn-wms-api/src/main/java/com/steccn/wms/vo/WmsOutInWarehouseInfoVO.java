package com.steccn.wms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsOutInWarehouseInfoVO {

    private String type;

    private Integer type1;

    //合计标识
    private Integer totalFlag = 0;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal thick;

    private String unit;

    private BigDecimal  morningShiftStorage;

    private BigDecimal  nightShiftStorage;

    private BigDecimal  totalInboundQuantity;

    private BigDecimal  morningShiftOutbound;

    private BigDecimal  NightShiftOutbound;

    private BigDecimal  totalOutboundQuantity;

    private String materialCode;

    private Long materialId;

    private Integer sortFlag;

    private Integer sortFlag1;

}
