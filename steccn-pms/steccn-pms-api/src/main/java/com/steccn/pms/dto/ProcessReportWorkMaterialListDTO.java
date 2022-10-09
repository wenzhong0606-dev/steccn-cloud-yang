package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 工序报工-物料消耗
 */
@Data
@ApiModel("工序报工-物料消耗vo")
public class ProcessReportWorkMaterialListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("组件id")
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
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("仓库id")
    private Long warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Long shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;
}
