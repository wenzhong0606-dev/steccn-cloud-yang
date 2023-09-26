package com.steccn.platform.dto;

import lombok.Data;

/**
 * 物流单详情DTO
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class SalesLogisticsInfoDTO {
    /**
     * 物流单号
     */
    private String logistics_code;
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
}
