package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 条码打印
 */
@Data
@ApiModel("条码打印的dto")
public class BarcodePrintDTO implements Serializable {

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("生产订单no")
    private String produceOrderNo;

    @ApiModelProperty("产线id")
    private Integer productionLine;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("打印条数")
    private Integer printStrip;

    @ApiModelProperty("打印张数")
    private Integer printNumber;

    @ApiModelProperty("班次id")
    private Integer classesId;

    @ApiModelProperty("班组id")
    private Integer classGroupId;
}
