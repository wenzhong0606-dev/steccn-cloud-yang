package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("基本下拉vo")
@AllArgsConstructor
public class BaseSelectVO<T> implements Serializable {

    @ApiModelProperty("id")
    private T id;

    @ApiModelProperty("名字")
    private String name;
}
