package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓条件dto
 */
@Data
@ApiModel("数仓条件dto")
public class DwConditionDTO implements Serializable {

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;



}
