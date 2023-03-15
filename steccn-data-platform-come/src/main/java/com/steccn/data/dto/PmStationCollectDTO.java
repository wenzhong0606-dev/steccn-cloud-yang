package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 生产管理-进出站采集dto
 */
@Data
@ApiModel("生产管理-进出站采集dto")
public class PmStationCollectDTO implements Serializable {

    @ApiModelProperty("设备编码")
    private String code;

    @ApiModelProperty("时间戳")
    private LocalDateTime timestamp;

    @ApiModelProperty("计数值")
    private BigDecimal value;
}
