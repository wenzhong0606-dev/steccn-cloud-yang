package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 原片用量
 * @date 2023/7/21
 */
@Data
@ApiModel("原片用量")
public class PmsUseCountDTO implements Serializable {

    @ApiModelProperty("产线")
    private String productLineId;

    @ApiModelProperty("月份")
    private String monthStr;

    @ApiModelProperty("工位")
    private Integer stationId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

}
