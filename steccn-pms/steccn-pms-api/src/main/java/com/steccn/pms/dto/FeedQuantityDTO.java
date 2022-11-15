package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 投料信息DTO
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@Data
@ApiModel("投料信息DTO")
public class FeedQuantityDTO {

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;
}
