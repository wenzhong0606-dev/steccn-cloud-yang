package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 工序报工-加工时间
 */
@Data
@ApiModel("工序报工-加工时间")
public class ProcessReportWorkHourDTO implements Serializable {

    @ApiModelProperty("原因（字典：工时原因）")
    private Long causeId;

    @ApiModelProperty("工时")
    private BigDecimal manHour;

    @ApiModelProperty("工时单位（字典时间单位）")
    private Long unitId;

}
