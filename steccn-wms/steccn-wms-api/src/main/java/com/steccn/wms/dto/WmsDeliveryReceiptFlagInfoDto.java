package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("出库信息标识DTO")
public class WmsDeliveryReceiptFlagInfoDto {


    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("数量")
    private BigDecimal quantity;
}
