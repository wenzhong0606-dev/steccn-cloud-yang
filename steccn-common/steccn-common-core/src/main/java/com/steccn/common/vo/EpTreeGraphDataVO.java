package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @desc 树图的data属性
 * @author yequan
 */
@Data
@ApiModel("树图的data属性")
@AllArgsConstructor
@NoArgsConstructor
public class EpTreeGraphDataVO implements Serializable {

    @ApiModelProperty("名称")
    private String name;
}
