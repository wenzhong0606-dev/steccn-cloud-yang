package com.steccn.wms.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("销售退货")
public class WmsSalesReturnsDTO {


    /**
     * 来源单号（物流单）
     */
    private String sourceCode;

    /**
     * 货架id
     */
    private Integer warehouseId;

    private List<WmsStorageReceiptInfoDTO> list;
}
