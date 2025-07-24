package com.steccn.wms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * erp库存对比
 *
 * @author AndrewLuo
 * @date 2025/07/21
 */
@Data
public class WmsInventoryCompareVO {
    /**
     * 仓库名称
     */
    private String stockName;
    /**
     * 物料编码
     */
    private String materialCode;
    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 金蝶库存
     */
    private BigDecimal erpQuantity;

    /**
     * 可用库存
     */
    private BigDecimal availQuantity;

    /**
     * 在途库存
     */
    private BigDecimal stockQuantity;

    /**
     * 总库存
     */
    private BigDecimal totalQuantity;

    /**
     * 差额
     */
    private BigDecimal difference;
}
