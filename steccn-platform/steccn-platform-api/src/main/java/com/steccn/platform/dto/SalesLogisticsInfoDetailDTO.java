package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 物流单详情DTO
 *
 * @author AndrewLuo
 * @date 2023/12/12
 */
@Data
public class SalesLogisticsInfoDetailDTO {
    /**
     * 状态 默认传 已出库
     */
    private String state;
    /**
     * 出库数量
     */
    private Integer out_qty;
    /**
     * 客户编码
     */
    private String customer_code;
    /**
     * 出库单号
     */
    private String delivery_code;
    /**
     * 客户条码
     */
    private List<String> customer_barcode;

    /**
     * 物流单号
     */
    private String logistics_code;
}
