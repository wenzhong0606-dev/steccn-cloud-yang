package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Data
@ApiModel("备件信息")
public class EqpSpareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("备件id")
    private Long id;

    @ApiModelProperty("备件编码")
    private String code;

    @ApiModelProperty("备件名称")
    private String name;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("采购类型id")
    private Long procurementTypeId;

    @ApiModelProperty("采购类型名称")
    private String procurementTypeName;

    @ApiModelProperty("长度单位")
    private Long lengthUnitId;

    @ApiModelProperty("长度")
    private BigDecimal lengthQuantity;

    @ApiModelProperty("高度单位")
    private Long highUnitId;

    @ApiModelProperty("高度")
    private BigDecimal highQuantity;

    @ApiModelProperty("宽度单位")
    private Long wideUnitId;

    @ApiModelProperty("宽度")
    private BigDecimal wideQuantity;

    @ApiModelProperty("规格")
    private String specifications;

    private BigDecimal thickQuantity;

    private Long thickUnitId;
}
