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
}
