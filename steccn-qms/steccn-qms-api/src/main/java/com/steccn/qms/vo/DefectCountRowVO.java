package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 不良周报表
 * @date 2023/7/27
 */
@Data
@ApiModel("不良周|月报表")
public class DefectCountRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("type 0-数据 1-合计（每周） 2-总计")
    private Integer type;

    @ApiModelProperty("周,月份")
    private String timeStr;

    @ApiModelProperty("产线")
    private String productLineName;

    @ApiModelProperty("原片不良")
    private BigDecimal originDefectNum;

    @ApiModelProperty("原片不良率")
    private BigDecimal originDefectRate;

    @ApiModelProperty("漏检")
    private BigDecimal missNum;

    @ApiModelProperty("漏检率")
    private BigDecimal missRate;

    @ApiModelProperty("磨边不良")
    private BigDecimal edgeDefectNum;

    @ApiModelProperty("磨边不良率")
    private BigDecimal edgeDefectRate;

    @ApiModelProperty("钻孔不良")
    private BigDecimal drillDefectNum;

    @ApiModelProperty("钻孔不良率")
    private BigDecimal drillDefectRate;

    @ApiModelProperty("丝印不良")
    private BigDecimal silkDefectNum;

    @ApiModelProperty("丝印不良率")
    private BigDecimal silkDefectRate;

    @ApiModelProperty("镀膜不良")
    private BigDecimal coatDefectNum;

    @ApiModelProperty("镀膜不良率")
    private BigDecimal coatDefectRate;

    @ApiModelProperty("钢化不良")
    private BigDecimal temperDefectNum;

    @ApiModelProperty("钢化不良率")
    private BigDecimal temperDefectRate;

    @ApiModelProperty("使用数")
    private BigDecimal useCountNum;

    @ApiModelProperty("成品数")
    private BigDecimal inStorageNum;

    @ApiModelProperty("合计不良")
    private BigDecimal totalDefectNum;

    @ApiModelProperty("合计不良率")
    private BigDecimal totalDefectRate;

    @ApiModelProperty("合计深加工不良")
    private BigDecimal totalDeepDefectNum;

    @ApiModelProperty("合计深加工不良率")
    private BigDecimal totalDeepDefectRate;

}
