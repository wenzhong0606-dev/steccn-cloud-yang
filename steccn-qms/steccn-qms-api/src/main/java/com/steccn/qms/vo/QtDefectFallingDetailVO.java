package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 落板明细信息
 * @date 2023/7/11
 */
@Data
@ApiModel("落板明细信息")
public class QtDefectFallingDetailVO implements Serializable {

    @ApiModelProperty("落板")
    private String beginStr;

    @ApiModelProperty("收板")
    private String overStr;

    @ApiModelProperty("落板时长")
    private BigDecimal interval;

    @ApiModelProperty("落板时长（小时）")
    private BigDecimal intervalHour;

    @ApiModelProperty("落板原因")
    private String reason;

}
