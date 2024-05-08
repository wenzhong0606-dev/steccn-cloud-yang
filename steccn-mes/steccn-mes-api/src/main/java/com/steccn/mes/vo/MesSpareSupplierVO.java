package com.steccn.mes.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 备品备件供应商VO
 *
 * @author AndrewLuo
 * @date 2024/05/07
 */
@Data
public class MesSpareSupplierVO {
    private String supplierCompanyCode;
    private String supplierCompanyName;
    private BigDecimal prQuantity;
}
