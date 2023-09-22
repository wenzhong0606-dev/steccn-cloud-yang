package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("通过物流单获取的物流明细")
public class WmsDeliverySellInfoVO {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("客户")
    private String customerName;

    @ApiModelProperty("发货数量/片")
    private BigDecimal saleQty;
}
