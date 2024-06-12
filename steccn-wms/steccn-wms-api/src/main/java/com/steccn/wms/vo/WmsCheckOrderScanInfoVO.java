package com.steccn.wms.vo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsCheckOrderScanInfoVO {

    /**
     * 物料id
     */

    private Long materialId;

    /**
     * 物料code
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 备注
     */
    private String note;


    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 仓库
     */
    private Integer warehouse;

    private String warehouseName;

    /**
     * 货架
     */
    private Integer shelf;

    private String shelfName;

    private String unitName;



}
