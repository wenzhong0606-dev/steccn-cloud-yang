package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/12 13:46
 */
@Data
public class PassRateOverViewVO {

    @ApiModelProperty("合格率")
    private String passRate;

    /**
     * 日环比%
     */
    @ApiModelProperty("日环比")
    private String dayValue;

    /**
     * 周同比%
     */
    @ApiModelProperty("周同比")
    private String weekValue;

    /**
     * 最近7天合格率
     */
    @ApiModelProperty("最近7天合格率")
    private List<BigDecimal> datePassList;
}
