package com.steccn.wms.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;



@Data
public class WmsSignFeedbackVO {

    private Long id;

    /**
     * 物流单号
     */
    private String salesOrder;

    /**
     * 客户
     */
    private String client;


    /**
     * 反馈状态（0:待确认反馈 1：已反馈）
     */
    private Integer state;

    /**
     * 发货日期
     */
    private LocalDate sendDate;

    /**
     * 发货数量
     */
    private Integer sendQuantity;

    /**
     * 发货面积
     */
    private Integer priceQuantity;

    /**
     * 签收类型
     */
    private String signatureType;

    /**
     * 签收数量
     */
    private Integer signedQuantity;

    /**
     * 签收面积
     */
    private Integer signedPriceQuantity;

    /**
     * 签收日期
     */
    private LocalDate signedDate;

    /**
     * 异常原因
     */
    private String abnormalCause;

    /**
     * 文件id
     */
    private Long fileId;


    private String fileUrl;

    private String carCode;

    private String product;

    private  String logisticsCompany;
}
