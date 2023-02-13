package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
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
    @DecimalMin(value="0",message="数量应大于0")
    @DecimalMax(value="99999999.999",message="数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    @NotNull(message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("打印条数")
    @NotNull(message = "打印条数不能为空")
    @Max(value = 500,message = "打印条数最大为500")
    @Min(value = 1,message = "打印条数最小为1")
    private Integer printStrip;

    @ApiModelProperty("打印张数")
    @NotNull(message = "打印张数不能为空")
    @Max(value = 500,message = "打印张数最大为500")
    @Min(value = 1,message = "打印张数最小为1")
    private Integer printNumber;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("班组id")
    private Long classGroupId;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("货架id")
    private Integer shelfId;
}
