package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领料计划VO
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@Data
@ApiModel("领料计划VO")
public class MaterialRequisitionPlanVO {

    @ApiModelProperty("主键")
    private Long id;
    @ApiModelProperty("批次送货量")
    private BigDecimal batchDeliveryQuantity;
    @ApiModelProperty("线边数量")
    private String lineEdgeQuantity;
    @ApiModelProperty("物料编码")
    private String materialCode;
    @ApiModelProperty("物料id")
    private String materialId;
    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("在途数量")
    private String onPassageQuantity;

    @ApiModelProperty("产线id")
    private String productionLine;
    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("需求时间")
    private String requireDate;

    @ApiModelProperty("需求数量")
    private String requireQuantity;

    @ApiModelProperty("工位")
    private String station;

    @ApiModelProperty("工位名称")
    private String stationName;

    @ApiModelProperty("单位id")
    private String unitId;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("车间id")
    private String workshop;

    @ApiModelProperty("车间名称")
    private String workshopName;
}
