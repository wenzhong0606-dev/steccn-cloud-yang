package com.steccn.wms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 销售入库单新增
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesStorageReceiptDTO extends WmsStorageReceiptDTO implements Serializable {
    /**
     * 供应商编码
     */
    @NotBlank(message = "供应商编码不能为空")
    private String esSupplierCode;
    /**
     * 收货单编码
     */
    @NotBlank(message = "收货单编码不能为空")
    private String deliveryOrderCode;

}
