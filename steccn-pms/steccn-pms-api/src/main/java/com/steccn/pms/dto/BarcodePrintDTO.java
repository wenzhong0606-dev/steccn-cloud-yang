package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotNull(message = "生产订单主键不能为空")
    private Long produceOrderId;

    @ApiModelProperty("生产订单no")
    @NotBlank(message = "生产订单编码不能为空")
    private String produceOrderNo;

    @ApiModelProperty("产线id")
    @NotNull(message = "产线主键不能为空")
    private Integer productionLine;

    @ApiModelProperty("物料id")
    @NotNull(message = "物料不能为空")
    private Long materialId;

    @ApiModelProperty("批次号")
    @NotBlank(message = "生产订单批次号不能为空")
    private String batchNo;

    @ApiModelProperty("数量")
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty("打印条数")
    @NotNull(message = "打印条数不能为空")
    private Integer printStrip;

    @ApiModelProperty("打印张数")
    @NotNull(message = "打印张数不能为空")
    private Integer printNumber;

    @ApiModelProperty("班次id")
    private Integer classesId;

    @ApiModelProperty("班组id")
    private Integer classGroupId;
}
