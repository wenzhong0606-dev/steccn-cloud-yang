package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 物料工位信息DTO
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@ApiModel("物料工位信息")
@Data
public class BomStationDTO {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("执行工位编码")
    private String executeStationCode;

    @ApiModelProperty("执行工位id")
    private Long executeStationId;

    @ApiModelProperty("执行工位名称")
    private String executeStationName;
}
