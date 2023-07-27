package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分录明细实体
 *
 * @author AndrewLuo
 * @date 2023/07/27
 */
@Data
public class ErpTransferSaveDetailDTO implements Serializable {
    /**
     * mes分录ID
     */
    private String fmesentryid;
    /**
     * 物料编码
     */
    private String fmatno;
    /**
     * 调拨数量
     */
    private BigDecimal fqty;
    /**
     * 调出仓库
     */
    private String fsrcstockno;
    /**
     * 调入仓库
     */
    private String fdeststockno;
    /**
     * 批号
     */
    private String flot;
}
