package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel("销售出库标识VO")
public class WmsDeliverySellFlagVO implements Serializable {
    /**
     * 单号是否存在
     */
    private Integer number = 0;

    /**
     * 状态为草稿/已预约/已同步锁/已入场
     */
    private Integer createStateFlag = 0;

    private Integer leaveTheFactoryStateFlag = 0;

    private Integer feedbackFlag = 0;

    private Integer companyFlag=0;


    /**
     * 合同未发货>0
     */
    private Integer contractNotDeliveredFlag = 0;

    private Integer makeAnAppointmentFlag = 0;

    private String salesOrder;

    private String client;

    private LocalDate sendDate;

    private BigDecimal sendQuantity;

    private BigDecimal priceQuantity;

    private String carCode;

    private String product;

    private String logisticsCompany;

    private static final long serialVersionUID = 1L;

}
