package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 检验项停用启用
 */
@Data
@ApiModel("检验项停用启用dto")
public class QtCheckItemStatusDTO implements Serializable {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("状态：0停用  1启用")
    private Integer status;
}
