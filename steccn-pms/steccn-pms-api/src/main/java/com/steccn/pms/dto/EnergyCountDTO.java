package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗统计dto")
public class EnergyCountDTO implements Serializable {

    @ApiModelProperty("时间范围类型")
    private Integer dateRangeType;

    @ApiModelProperty("生产车间id")
    private Integer workshopId;


}
