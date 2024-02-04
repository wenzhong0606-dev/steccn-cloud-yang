package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author mengle
 */
@Data
@ApiModel("深加工日报表不良数数据库查询vo")
public class FurtherProduceReportBadnessVO implements Serializable {

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("产品id")
    private Long productId;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("不合格数")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("耗用数")
    private BigDecimal consumptionQuantity;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("周")
    private String weekStr;
}
