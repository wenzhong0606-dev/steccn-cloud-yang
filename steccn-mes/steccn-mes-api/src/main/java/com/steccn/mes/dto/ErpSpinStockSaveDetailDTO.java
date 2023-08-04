package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 保存简单生产入库单
 *
 * @author AndrewLuo
 * @date 2023/07/28
 */
@Data
public class ErpSpinStockSaveDetailDTO implements Serializable {
    /**
     * mes分录id
     */
    private String fmesentryid;
    /**
     * 物料编码
     */
    private String fmatno;
    /**
     * 应收数量
     */
    private BigDecimal fmustqty;
    /**
     * 实收数量
     */
    private BigDecimal frealqty;
    /**
     * 仓库编码
     */
    private String fstockno;
    /**
     * 生产车间编码
     */
    private String fdeptno;

    /**
     * 批号
     */
    private String flot;

}
