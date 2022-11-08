package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@Data
@ApiModel("基片车间数据vo")
public class SubstrateDataListVO implements Serializable {

    @ApiModelProperty("0完工 1入库")
    private Integer type;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("班次")
    private Long classesId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

}
