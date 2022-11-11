package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author yequan
 */
@ApiModel("基片日报表-原料耗料")
@Data
public class SubstrateIngredientVO implements Serializable {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("领料数量(Kg)")
    private BigDecimal inKg;

    @ApiModelProperty("领料数量(t)")
    private BigDecimal inTon;

    @ApiModelProperty("投料数量(Kg)")
    private BigDecimal outKg;

    @ApiModelProperty("投料数量(t)")
    private BigDecimal outTon;

    @ApiModelProperty("生产订单id")
    private Set<Long> produceOrderIdSet;

}
