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
 * @desc 工序报工-物料消耗
 */
@Data
@ApiModel("工序报工-物料消耗vo")
public class ProcessReportWorkMaterialListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("库存id")
    private Long inventoryId;

    @ApiModelProperty("生产订单id")
    @NotNull(message = "生产订单不能为空")
    private Long produceOrderId;

    @ApiModelProperty("组件id")
    @NotNull(message = "组件不能为空")
    private Long moduleId;

    @ApiModelProperty("组件编码")
    private String moduleCode;

    @ApiModelProperty("组件名称")
    private String moduleName;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("数量 = 订单BOM组件数量 / 订单数量 * 加工数量 可修改")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value="0",message="数量应大于0")
    @DecimalMax(value="99999999.999",message="数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("可用数量")
//    @NotNull(message = "可用数量不能为空")
//    @DecimalMin(value="0",message="可用数量应大于0")
//    @DecimalMax(value="99999999.999",message="可用数量大小应小于99999999.999")
//    @Digits(integer = 8,fraction = 3,message = "可用数量整数位上限为8位，小数位上限位3位")
    private BigDecimal availableQuantity;

    @ApiModelProperty("不良数量")
    @NotNull(message = "不良数量不能为空")
    @DecimalMin(value="0",message="不良数量应大于0")
    @DecimalMax(value="99999999.999",message="不良数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "不良不良数量整数位上限为8位，小数位上限位3位")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("打包时间")
    private LocalDateTime packTime;
}
