package com.steccn.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物流单VO
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class SalesLogisticsVO {
    /**
     * 物流单号
     */
    private String name;

    /**
     * 发货通知单号
     */
    private String send_notice_code;

    /**
     * 客户名称
     */
    private String customer_name;

    /**
     * 客户合同号
     */
    private String customer_code;

    /**
     * 客户订单号
     */
    private String product_name;

    private String product_code;
    private String model_specs;
    private LocalDate send_date;

    /**
     * 发货数量 片
     */
    private Integer sale_qty;

    /**
     * 面积 平方米
     */
    private BigDecimal price_qty;
    private String driver_adds;
    private BigDecimal weight;
    private BigDecimal delivery_freight;
    private String pack_type;
    private String warehouse;
    private String contract_code;
    private Integer contract_qty;

    /**
     * 状态  草稿 已预约 已同步 已出库 已反馈 已退货 已补发
     */
    private String state;
}
