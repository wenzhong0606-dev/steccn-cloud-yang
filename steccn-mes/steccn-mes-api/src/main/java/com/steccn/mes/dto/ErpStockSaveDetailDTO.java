package com.steccn.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购入库单DTO
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class ErpStockSaveDetailDTO extends ErpSpinStockSaveDetailDTO{
    /**
     * 含税单价
     */
    private BigDecimal ftaxprice;

    /**
     * 税率
     */
    private BigDecimal fentrytaxrate;

    /**
     * mes收料通知单单据id
     */
    private String fsrcmesbillid;

    /**
     * mes收料通知单单据编号
     */
    private String fsrcmesbillno;

    /**
     * mes收料通知单分录id
     */
    private String fsrcmesentryid;

    /**
     * 销售退货单使用的实退数量
     */
    private BigDecimal fqty;
}
