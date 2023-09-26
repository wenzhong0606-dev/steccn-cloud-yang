package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 销售入库单新增
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesStorageReceiptDTO extends WmsStorageReceiptDTO implements Serializable {
    private String esSupplierCode;
}
