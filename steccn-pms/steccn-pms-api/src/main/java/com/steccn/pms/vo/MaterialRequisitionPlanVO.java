package com.steccn.pms.vo;

import com.steccn.common.util.LocalDateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private BigDecimal lineEdgeQuantity;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("在途数量")
    private BigDecimal onPassageQuantity;

    @ApiModelProperty("产线id")
    private Integer productionLine;

    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("需求时间")
    private LocalDate requireDate;

    @ApiModelProperty("需求数量")
    private BigDecimal requireQuantity;

    @ApiModelProperty("工位")
    private Integer station;

    @ApiModelProperty("工位编码")
    private String stationCode;

    @ApiModelProperty("工位名称")
    private String stationName;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("车间id")
    private Integer workshop;

    @ApiModelProperty("车间名称")
    private String workshopName;

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;
}
