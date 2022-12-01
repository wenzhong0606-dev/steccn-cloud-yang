package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能源流向树图的vo
 */
@Data
@ApiModel("能源流向树图的vo")
public class EpTreeGraphVO implements Serializable {

    @ApiModelProperty("data属性")
    private List<EpTreeGraphDataVO> data;

    @ApiModelProperty("links属性")
    private List<EpTreeGraphLinkVO> links;

}
