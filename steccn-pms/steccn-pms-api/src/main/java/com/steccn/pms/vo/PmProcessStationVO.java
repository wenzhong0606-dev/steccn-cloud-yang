package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 执行工位信息
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("执行工位信息")
public class PmProcessStationVO {

    @ApiModelProperty("执行工位名称")
    private String executeStationName;

    @ApiModelProperty("执行工位id")
    private Integer executeStationId;
}
