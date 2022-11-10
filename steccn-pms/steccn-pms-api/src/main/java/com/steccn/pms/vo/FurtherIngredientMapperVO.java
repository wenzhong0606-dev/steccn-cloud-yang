package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@ApiModel("深加工原料耗料vo")
@Data
public class FurtherIngredientMapperVO implements Serializable {

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("班次id")
    private Long classesId;

}
