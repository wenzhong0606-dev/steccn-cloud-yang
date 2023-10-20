package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("出库信息标识")
public class WmsDeliveryFlagVO {

    @ApiModelProperty("箱号在库")
    private Integer caseNoFlag = 0;

    @ApiModelProperty("箱号对应的物料与物流单中的物料相同")
    private Integer materialEqualFlag = 0;

    @ApiModelProperty("箱号对应的库存明细中可用数量>0")
    private Integer caseNumberQuantityFlag = 0;

    @ApiModelProperty("箱号是否绑定客户条码")
    private Integer caseNoBindingCustomerBarcodeFlag = 0;

}
