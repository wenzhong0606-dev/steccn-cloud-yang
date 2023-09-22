package com.steccn.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售系统收货单VO
 *
 * @author AndrewLuo
 * @date 2023/09/14
 */
@Data
public class WmsSalesDeliveryTransferVO {
    private Integer asnLineNum;
    private String esAsnLineNum;
    private String esPoNum;
    private String esPoLineNum;
    private String esPoLineLocationNum;
    private LocalDateTime neededDate;
    private String esAgentCode;
    private String esItemCode;
    private String esItemName;
    private String esUomCode;
    private BigDecimal shipQuantity;
    private BigDecimal receiveQuantity;
    private BigDecimal returnToSupplierQuantity;
    private String receiveStatus;
    private Integer cancelledFlag;
    private Integer closedFlag;
    private LocalDateTime closedDate;
    private Integer closedBy;
    private String closedRemark;
    private LocalDateTime promisedDate;
}
