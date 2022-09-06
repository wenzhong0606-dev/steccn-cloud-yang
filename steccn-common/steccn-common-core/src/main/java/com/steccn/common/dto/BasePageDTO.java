package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 基础分页dto
 */
@Data
@ApiModel(value = "基础分页dto")
public class BasePageDTO implements Serializable {

    @ApiModelProperty(value = "起始页")
    private Integer current = 1;

    @ApiModelProperty(value = "分页大小")
    private Integer size = 20;

    @ApiModelProperty("排序字段")
    private String sortField;

    @ApiModelProperty("升序降序规则: true升序 false降序")
    private Boolean ascFlag = Boolean.TRUE;
}
