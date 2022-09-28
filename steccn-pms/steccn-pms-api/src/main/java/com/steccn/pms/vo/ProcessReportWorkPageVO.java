package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("工序报工分页vo")
public class ProcessReportWorkPageVO {

    @ApiModelProperty("报工号")
    private String code;

    @ApiModelProperty("生产订单编码")
    private String produceOrderNo;

    @ApiModelProperty("工序编码")
    private String processNo;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工位")
    private String stationNo;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("合格数")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("单位")
    private String unitName;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("班次名称")
    private String classesName;
}
