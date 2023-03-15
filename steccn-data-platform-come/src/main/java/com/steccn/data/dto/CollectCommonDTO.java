package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 采集点公共dto
 */
@Data
@ApiModel("采集点公共dto")
public class CollectCommonDTO<T> implements Serializable {

    @ApiModelProperty("采集点公共dto")
    private List<T> data;
}
