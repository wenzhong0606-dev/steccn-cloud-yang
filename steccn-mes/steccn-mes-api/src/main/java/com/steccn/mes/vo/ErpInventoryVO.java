package com.steccn.mes.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存VO
 *
 * @author AndrewLuo
 * @date 2025/07/21
 */
@Data
public class ErpInventoryVO {
    /**
     * 物料编码
     */
    private String fmatno;

    /**
     * 物料名称
     */
    private String fmatname;

    /**
     * 仓库编码
     */
    private String fstockno;

    /**
     * 仓库名称
     */
    private String fstockname;

    /**
     * 数量
     */
    private BigDecimal fqty;

    /**
     * 批号
     */
    private String flot;
}
