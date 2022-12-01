package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 树图link的视图对象
 */
@Data
@ApiModel("树图link的视图对象")
@AllArgsConstructor
@NoArgsConstructor
public class EpTreeGraphLinkVO implements Serializable {

    @ApiModelProperty("来源")
    private String source;

    @ApiModelProperty("目标")
    private String target;

    @ApiModelProperty("值")
    private BigDecimal value;

}
