package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;


@Data
public class WmsSignFeedbackDTO extends BasePageDTO implements Serializable {

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
     *     物流公司
     */
    private String logisticsCompany;



    /**
     * 反馈状态（0:待确认反馈 1：已反馈）
     */
    private Integer state;

    private String carCode;

    private String product;

    /**
     * 发货日期
     */
    private LocalDate sendDate;

    /**
     * 发货数量
     */
    private BigDecimal sendQuantity;

    /**
     * 发货面积
     */
    private BigDecimal priceQuantity;

    /**
     * 签收类型
     */
    private String signatureType;

    /**
     * 签收数量
     */
    private BigDecimal signedQuantity;

    /**
     * 签收面积
     */
    private BigDecimal signedPriceQuantity;

    /**
     * 签收日期
     */
    private Date signedDate;

    /**
     * 异常原因
     */
    private String abnormalCause;

    /**
     * 文件id
     */
    private Long fileId;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate sendStartDate;

    private LocalDate sendEndDate;

    /**
     * 退回原因
     */
    private String sendBackReason;
}
