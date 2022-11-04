package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗统计
 */
@ApiModel("能耗统计vo")
@Data
public class EnergyCountListVO implements Serializable {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("数据")
    private List<BigDecimal> data;

    @ApiModelProperty("类型 line bar")
    private String type;

    @ApiModelProperty("水电油气")
    private String stack;

}
