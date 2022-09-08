package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 工艺路线工序VO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线工序VO")
public class PmCraftRouteProcessVO implements Serializable {

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;


}
