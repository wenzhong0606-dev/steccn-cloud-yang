package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@Data
@ApiModel("出库单的物料数据")
public class MessOutStorageVoucherListDTO implements Serializable {

    @ApiModelProperty("出库单id")
    private Long inventoryId;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("实际数量")
    private BigDecimal quantity;

}
