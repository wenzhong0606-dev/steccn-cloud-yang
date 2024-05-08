package com.steccn.platform.dto;

import lombok.Data;

/**
 * 退货单同步DTO
 *
 * @author AndrewLuo
 * @date 2024/05/06
 */
@Data
public class SalesReturnOrderSyncDTO {
    /**
     * 请求服务编号
     */
    private String server_code;
    /**
     * 物流单号
     */
    private String logistics_code;
    /**
     * 入库单号
     */
    private String stock_order;
}
