package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/11/7 15:53
 */
@Data
public class ProduceOrderTrackReportVO {

    @ApiModelProperty("产线id")
    private Integer productionLineId;

    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("订单号")
    private String code;

    @ApiModelProperty("产品编码")
    private String materialCode;

    @ApiModelProperty("产品名称")
    private String materialName;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("计划数")
    private BigDecimal planQuantity;

    @ApiModelProperty("计划数平方")
    private BigDecimal planQuantitySq;

    @ApiModelProperty("计划开始时间")
    private LocalDateTime actualStartTime;

    @ApiModelProperty("计划结束时间")
    private LocalDateTime actualEndTime;

    @ApiModelProperty("完成数")
    private BigDecimal completeQuantity;

    @ApiModelProperty("完成数平方")
    private BigDecimal completeQuantitySq;

    @ApiModelProperty("差额数")
    private BigDecimal shortfallQuantity;

    @ApiModelProperty("差额数平方")
    private BigDecimal shortfallQuantitySq;

    @ApiModelProperty("订单状态")
    private Integer state;

    @ApiModelProperty("备注")
    private String note;
}
