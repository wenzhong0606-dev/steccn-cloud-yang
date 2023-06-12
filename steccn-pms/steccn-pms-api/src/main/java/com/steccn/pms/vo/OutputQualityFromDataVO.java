package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApiModel("基片产量质量报工数据")
@Data
public class OutputQualityFromDataVO {

    @ApiModelProperty("产线名称")
    private String produceLineName;

    @ApiModelProperty("班次")
    private String className;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("时长")
    private BigDecimal duration;

    @ApiModelProperty("时长单位id")
    private Long unitId;

    @ApiModelProperty("时长单位")
    private String unitName;

    @ApiModelProperty("原因")
    private String cause;

    @ApiModelProperty("原因id")
    private Long causeId;

    @ApiModelProperty("备注")
    private String remarks;


}
