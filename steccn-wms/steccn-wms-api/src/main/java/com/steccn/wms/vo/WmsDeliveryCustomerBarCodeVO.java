package com.steccn.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 出库单关联的客户条码
 *
 * @author AndrewLuo
 * @date 2023/10/31
 */
@Data
public class WmsDeliveryCustomerBarCodeVO implements Serializable {
    /**
     * 物流单code
     */
    private String logisticsCode;
    /**
     * 出库单号
     */
    private String deliveryCode;
    /**
     * 客户条码
     */
    private List<String> customerBarCode;
}
