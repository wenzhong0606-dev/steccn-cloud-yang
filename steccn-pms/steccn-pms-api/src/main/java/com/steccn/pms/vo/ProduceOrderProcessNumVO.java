package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@ApiModel("生产订单工序vo")
@Data
public class ProduceOrderProcessNumVO implements Serializable {

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("使用数量")
    private BigDecimal quantity;

    @ApiModelProperty("计划数量")
    private BigDecimal avaQuantity;
}
