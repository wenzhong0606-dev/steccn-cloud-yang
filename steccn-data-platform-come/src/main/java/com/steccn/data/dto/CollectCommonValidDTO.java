package com.steccn.data.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * 采集点公共dto 带校验
 *
 * @author AndrewLuo
 * @date 2023/04/26
 */
@Data
public class CollectCommonValidDTO<T> implements Serializable {
    @ApiModelProperty("采集点公共dto")
    @Valid
    private List<T> data;
}
