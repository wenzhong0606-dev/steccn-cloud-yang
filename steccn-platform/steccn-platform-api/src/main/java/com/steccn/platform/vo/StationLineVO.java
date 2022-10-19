package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("工位和产线vo")
public class StationLineVO implements Serializable {

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("工位名字")
    private String stationName;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("产线名称")
    private String productLineName;
}
