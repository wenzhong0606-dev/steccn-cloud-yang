package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("消息tab的vo")
public class SysMsgTabVO implements Serializable {

    @ApiModelProperty("分类-大类")
    private Integer category;

    @ApiModelProperty("tab名称")
    private String name;

    @ApiModelProperty("数量")
    private Integer count;
}
