package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/12 13:46
 */
@Data
public class PassRateOverViewVO implements Serializable {

    @ApiModelProperty("合格率")
    private String value;

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
    private List<BigDecimal> yData;

    /**
     * 横坐标
     */
    private List<LocalDate> xData;

    @ApiModelProperty("目标值")
    private BigDecimal targetValue;

    /**
     * 日环比  0 下降
     *        1 上升
     */
    private Integer dayFlag;

    /**
     * 周同比 0 下降
     *       1 上升
     */
    private Integer weekFlag;

    private Integer key;

    private String title;

    private String tooltip;
}
