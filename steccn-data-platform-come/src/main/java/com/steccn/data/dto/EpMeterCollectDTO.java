package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 能源绩效-远传仪表采集dto
 */
@Data
@ApiModel("能源绩效-远传仪表采集dto")
public class EpMeterCollectDTO implements Serializable {

    @ApiModelProperty("设备编码")
    private String code;

    @ApiModelProperty("时间戳")
    private LocalDateTime timestamp;

    @ApiModelProperty("抄表值")
    private BigDecimal value;
}
