package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 执行工位信息
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("执行工位信息")
public class PmProcessStationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("执行工位名称")
    private String executeStationName;

    @ApiModelProperty("执行工位id")
    private String executeStationCode;
}
