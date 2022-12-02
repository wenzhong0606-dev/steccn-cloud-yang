package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("标准值 企业标准 | 行业标准")
public class StandardVO<T> implements Serializable {

    @ApiModelProperty("行业标准0 企业标准1")
    private Integer type;

    @ApiModelProperty("标准名称 企业标准|行业标准")
    private String name;

    @ApiModelProperty("标准值 可能是Integer 可能是BigDecimal 还有可能是其余引用类型")
    private T value;

}
