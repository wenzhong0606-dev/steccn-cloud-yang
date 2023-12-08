package com.steccn.dp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售系统客户看板DTO
 *
 * @author AndrewLuo
 * @date 2023/12/07
 */
@Data
public class SalesContractClassDTO {
    private String product_class;
    private BigDecimal qty;
    private BigDecimal ratio;
}
