package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("基片产量质量-深加工数据")
public class OutputQualitySjgDataVO {

    @ApiModelProperty("类型：0 是普通行 1 是合计行")
    private Integer type = 0;

    @ApiModelProperty("班次")
    private String classes;

    @ApiModelProperty("班组")
    private String classGroup;

    @ApiModelProperty("班长")
    private String monitor;

    @ApiModelProperty("产线")
    private String productLine;

    @ApiModelProperty("产线排序")
    private Integer sort;

    @ApiModelProperty("厚度")
    private BigDecimal thickQuantity;

    @ApiModelProperty("规格")
    private String specifications;

    @ApiModelProperty("原片不良")
    private Integer rawClassBadness;

    @ApiModelProperty("磨边不良")
    private Integer edgingBadness;

    @ApiModelProperty("打孔不良")
    private Integer punchingBadness;

    @ApiModelProperty("丝印不良")
    private Integer silkScreenBadness;

    @ApiModelProperty("钢化不良")
    private Integer temperingBadness;

    @ApiModelProperty("不良合计")
    private Integer badTotal;

    @ApiModelProperty("原片耗用量/片")
    private BigDecimal currentClassSlice;

    @ApiModelProperty("原片耗用量/平方米")
    private BigDecimal currentClassSquare;

    @ApiModelProperty("入库产量/片")
    private BigDecimal inStorageSlice;

    @ApiModelProperty("入库产量/平方米")
    private BigDecimal inStorageSquare;

    @ApiModelProperty("加工产量/片")
    private BigDecimal processQuantitySlice;

    @ApiModelProperty("加工产量/平方米")
    private BigDecimal processQuantitySquare;

    @ApiModelProperty("成品率")
    private BigDecimal finishedProduct;


}
