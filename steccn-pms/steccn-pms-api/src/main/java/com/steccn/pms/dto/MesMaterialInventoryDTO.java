package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * MES物料库存信息
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@ApiModel("MES物料库存信息")
@Data
public class MesMaterialInventoryDTO {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;
}
