package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 工艺路线工序DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线工序DTO")
public class PmCraftRouteProcessDTO implements Serializable {


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工艺路线id")
    private Long routeId;
}
