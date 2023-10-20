package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 完工入库扫描vo
 */
@Data
@ApiModel("完工入库扫描vo")
public class CompleteStorageScanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("箱号")
    @NotBlank(message = "箱号不能为空")
    private String caseNo;

    @ApiModelProperty("批次号")
    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    @ApiModelProperty("创建时间")
    @NotNull(message = "创建时间不能为空")
    private LocalDateTime createTime;

    @ApiModelProperty("箱状态")
    @NotNull(message = "箱状态不能为空")
    private Integer caseState;

    @ApiModelProperty("生产订单id")
    @NotNull(message = "生产订单主键不能为空")
    private Long produceOrderId;

    @ApiModelProperty("生产订单编号")
    @NotNull(message = "生产订单编号不能为空")
    private String produceOrderNo;

    @ApiModelProperty("数量")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value="0",message="数量应大于0")
    @DecimalMax(value="99999999.999",message="数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    @NotNull(message = "数量单位不能为空")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("仓库id")
    @NotNull(message = "仓库不能为空")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    @NotNull(message = "货架不能为空")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("物料id")
    @NotNull(message = "物料主键不能为空")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("入库时间")
    @NotNull(message = "入库时间不能为空")
    private LocalDateTime enterTime;

    @ApiModelProperty("客户条码")
    private String customerBarcode;

}
