package com.steccn.mes.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购系统申请数VO
 *
 * @author AndrewLuo
 * @date 2024/05/07
 */
@Data
public class SrmSupplierVO {
    private BigDecimal prQuantity;
    private String itemCode;
    private String itemName;
}
