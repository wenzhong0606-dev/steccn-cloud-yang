package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@ApiModel("基片日报表--原料耗料")
@Data
public class SubstrateIngredientConsumeVO implements Serializable {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料名称")
    private String materialCode;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("生产订单id集合")
    private String produceOrderIdStr;

}
