package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdsWmsInventoryVO {

    /**
     *
     */
    private Long materialId;

    /**
     *
     */
    private Integer warehouseId;

    /**
     *
     */
    private Integer shelfId;

    /**
     *
     */
    private Long unit1;

    /**
     *
     */
    private Date dt;

    /**
     *
     */
    private String materialCode;

    /**
     *
     */
    private String materialName;

    /**
     *
     */
    private String warehouseCode;

    /**
     *
     */
    private String warehouseName;

    /**
     *
     */
    private String shelfCode;

    /**
     *
     */
    private String shelfName;

    /**
     *
     */
    private BigDecimal totalQuantity1;

    /**
     *
     */
    private BigDecimal totalQuantity2;

    /**
     *
     */
    private Long unit2;

    /**
     *
     */
    private BigDecimal totalQuantity3;

    /**
     *
     */
    private Long unit3;
}
