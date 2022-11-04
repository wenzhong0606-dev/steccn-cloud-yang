package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 领料计划VO
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@ApiModel("领料计划VO")
@Data
public class PmsAcquisitionPlanVO implements Serializable {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("车间id")
    private Integer workshop;

    @ApiModelProperty("车间名称")
    private String workshopName;

    @ApiModelProperty("产线id")
    private Integer productionLine;

    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("工位")
    private Integer station;

    @ApiModelProperty("工位名称")
    private String stationName;

    @ApiModelProperty("物料id")
    private Integer materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("需求时间")
    private LocalDate requireDate;

    @ApiModelProperty("批次送货量")
    private BigDecimal batchDeliveryQuantity;

    @ApiModelProperty("线边数量")
    private BigDecimal lineEdgeQuantity;

    @ApiModelProperty("在途数量")
    private BigDecimal onPassageQuantity;

    @ApiModelProperty("需求数量")
    private BigDecimal requireQuantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;
}
