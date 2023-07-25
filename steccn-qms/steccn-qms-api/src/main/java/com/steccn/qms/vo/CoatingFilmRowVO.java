package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 镀膜不良月报表行视图对象
 * @date 2023/7/20
 */
@Data
@ApiModel("镀膜不良月报表行视图对象")
public class CoatingFilmRowVO implements Serializable {

    @ApiModelProperty("类型 0表示数据 1表示月份合计 2表示总计")
    private Integer type;

    @ApiModelProperty("月份")
    private String monthStr;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("值")
    private List<BigDecimal> valueList;

    @ApiModelProperty("使用数")
    private BigDecimal useCount;

    @ApiModelProperty("合计不良")
    private BigDecimal defectSum;

    @ApiModelProperty("合计不良率")
    private BigDecimal defectRate;


}
