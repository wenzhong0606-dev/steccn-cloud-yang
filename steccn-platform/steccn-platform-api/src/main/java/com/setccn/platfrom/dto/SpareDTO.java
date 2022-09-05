package com.setccn.platfrom.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Data
@ApiModel("备件信息")
public class SpareDTO {

    @ApiModelProperty("备件id")
    private Long id;

    @ApiModelProperty("备件编码")
    private String code;

    @ApiModelProperty("备件名称")
    private String name;

}
