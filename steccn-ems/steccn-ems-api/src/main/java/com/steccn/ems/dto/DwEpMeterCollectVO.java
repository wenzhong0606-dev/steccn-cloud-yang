package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓抄表数据vo
 */
@ApiModel
@Data
public class DwEpMeterCollectVO implements Serializable {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("记录时间")
    private LocalDateTime recordTime;

    @ApiModelProperty("值")
    private BigDecimal value;
}
