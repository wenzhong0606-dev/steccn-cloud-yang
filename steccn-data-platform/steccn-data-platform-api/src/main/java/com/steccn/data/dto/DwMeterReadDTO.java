package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @desc 数仓抄表dto
 * @author yequan
 */
@Data
@ApiModel("数仓抄表dto")
public class DwMeterReadDTO implements Serializable {

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;
}
