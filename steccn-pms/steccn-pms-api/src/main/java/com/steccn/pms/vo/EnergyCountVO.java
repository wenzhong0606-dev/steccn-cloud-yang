package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


/**
 * @author yequan
 * @desc 能耗统计返回vo
 */
@ApiModel
@Data
public class EnergyCountVO implements Serializable {

    @ApiModelProperty("x轴文字数据")
    private List<String> dx;

    @ApiModelProperty("拉引量")
    private List<BigDecimal> pullForceList;

    @ApiModelProperty("水、电、油、气")
    private EnergySeriesDataVO seriesData;

    @ApiModelProperty("维度总计-如果分组是班组，则三条。如果分组是产线，则条数为产线总数")
    private List<List<BigDecimal>> reactive;
}
