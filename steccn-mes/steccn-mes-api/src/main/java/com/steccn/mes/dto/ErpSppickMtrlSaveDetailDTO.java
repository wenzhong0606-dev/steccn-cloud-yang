package com.steccn.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 简单生产领料单详情DTO
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class ErpSppickMtrlSaveDetailDTO {
    /**
     * mes分录ID
     */
    private String fmesentryid;

    /**
     * 物料编码
     */
    private String fmatno;

    /**
     * 申请数量
     */
    private BigDecimal fappqty;

    /**
     * 实发数量
     */
    private BigDecimal factualqty;

    /**
     * 仓库编码
     */
    private String fstockno;

    /**
     * 批号
     */
    private String flot;

    /**
     * mes简单生产领料单单据id
     */
    private String fsrcmesbillid;

    /**
     * mes简单生产领料单单据编号
     */
    private String fsrcmesbillno;

    /**
     * mes简单生产领料单分录id
     */
    private String fsrcmesentryid;

}
