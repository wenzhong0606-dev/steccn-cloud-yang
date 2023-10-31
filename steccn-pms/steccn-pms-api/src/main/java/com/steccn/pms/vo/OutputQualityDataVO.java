package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("基片产量质量数据")
public class OutputQualityDataVO {

    @ApiModelProperty("类型：0 是普通行 1 是合计行")
    private Integer type = 0;

    @ApiModelProperty("班次")
    private String classes;

    @ApiModelProperty("班组")
    private String classGroup;

    @ApiModelProperty("班长")
    private String monitor;

    @ApiModelProperty("产线")
    private String productionLine;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("厚度")
    private BigDecimal thickQuantity;

    @ApiModelProperty("规格")
    private String specifications;

    @ApiModelProperty("闭口泡")
    private Integer shutUp;

    @ApiModelProperty("开口泡")
    private Integer startToTalk;

    @ApiModelProperty("结石掉落物")
    private Integer stone;

    @ApiModelProperty("辊印变形")
    private Integer variant;

    @ApiModelProperty("总疵点")
    private Integer flaw;

    @ApiModelProperty("当班拉引量")
    private BigDecimal currentClass;

    @ApiModelProperty("平均厚度")
    private BigDecimal averageThickness;

    @ApiModelProperty("产量/吨")
    private BigDecimal outputTon;

    @ApiModelProperty("产量/平方米")
    private BigDecimal outputSq;

    @ApiModelProperty("成品率")
    private BigDecimal finishedProduct;

    @ApiModelProperty("累计拉引量")
    private BigDecimal totalPull;

    @ApiModelProperty("累计产量")
    private String totalOutput;

    @ApiModelProperty("累计成品率")
    private BigDecimal totalFinishedProduct;


}
