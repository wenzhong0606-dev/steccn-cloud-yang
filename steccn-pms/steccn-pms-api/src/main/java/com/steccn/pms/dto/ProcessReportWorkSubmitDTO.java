package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("工序报工-提交报工")
public class ProcessReportWorkSubmitDTO implements Serializable {

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("加工数量")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数量")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("批次编码")
    private String batch;

    @ApiModelProperty("产线id")
    private Integer produceLineId;

    @ApiModelProperty("班次id")
    private Integer classesId;

    @ApiModelProperty("班组id")
    private Integer classGroupId;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("加工时间")
    private List<ProcessReportWorkHourDTO> hourDtoList;

    private static final long serialVersionUID = 1L;
}
