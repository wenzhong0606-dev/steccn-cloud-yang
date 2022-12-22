package com.steccn.data.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 生产管理-进出站计数器
 */
@ApiModel("生产管理-进出站计数器")
@Data
public class DwStationCollectVO implements Serializable {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("采集点编码")
    private String code;

    @ApiModelProperty("记录时间")
    private LocalDateTime recordTime;

    @ApiModelProperty("值")
    private BigDecimal value;
}
