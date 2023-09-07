package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * erp物料单位VO
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@Data
public class ErpMaterialUnitVO implements Serializable {
    /**
     * 物料编码
     */
    private String fnumber;
    /**
     * 基本单位编码
     */
    private String fbaseunitno;
    /**
     * 基本单位名称
     */
    private String fbaseunitname;
    /**
     * 基本单位换算率
     */
    private BigDecimal fbaserate;
    /**
     * 库存单位编码
     */
    private String fstockunitno;
    /**
     * 库存单位名称
     */
    private String fstockunitname;
    /**
     * 库存单位换算率
     */
    private BigDecimal fstockrate;

    /**
     * 销售单位编码
     */
    private String fsaleunitno;
    /**
     * 销售单位名称
     */
    private String fsaleunitname;
    /**
     * 销售单位换算率
     */
    private BigDecimal fsalerate;
    /**
     * 采购单位编码
     */
    private String fpurunitno;
    /**
     * 采购单位名称
     */
    private String fpurunitname;
    /**
     * 采购单位换算率
     */
    private BigDecimal fpurrate;
    /**
     * 生产单位编码
     */
    private String fprounitno;
    /**
     * 生产单位名称
     */
    private String fprounitname;
    /**
     * 生产单位换算率
     */
    private BigDecimal fprorate;
}
