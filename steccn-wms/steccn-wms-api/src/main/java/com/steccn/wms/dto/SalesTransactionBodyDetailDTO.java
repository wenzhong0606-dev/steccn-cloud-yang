package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售系统请求体事务明细
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesTransactionBodyDetailDTO implements Serializable {
    /**
     * 外部系统事务行编号
     */
    private String esTrxLineNum;
    /**
     * 事务日期
     */
    private String trxDate;
    /**
     * 外部系统接收事务类型代码
     */
    private String esTrxTypeCode;
    /**
     * 事务数量
     */
    private BigDecimal quantity;
    /**
     * 币种代码
     */
    private String stockType;

    private String currencyCode;
    /**
     * 外部系统所属业务实体代码
     */
    private String esOuCode;

    /**
     * 外部系统供应商代码
     */
    private String esSupplierNum;

    /**
     * 外部系统采购订单头编号
     */
    private String esFromPoNum;

    /**
     * 外部系统采购订单头行号
     */
    private String esFromPoLineNum;
}
