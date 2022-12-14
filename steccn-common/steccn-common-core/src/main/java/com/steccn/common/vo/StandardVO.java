package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ApiModel("标准值 企业标准 | 行业标准")
public class StandardVO<T> implements Serializable {

    @ApiModelProperty("标准id")
    private Integer id;

    @ApiModelProperty("产品厚度")
    private Long productThick;

    @ApiModelProperty("产品厚度")
    private BigDecimal thick;

    @ApiModelProperty("能源类型")
    private Integer energyType;

    @ApiModelProperty("行业标准 企业标准")
    private Long standardType;

    @ApiModelProperty("行业标准 企业标准")
    private String standardTypeName;

    @ApiModelProperty("产品类型")
    private Integer productType;

    @ApiModelProperty("产品类型-名称")
    private String productTypeName;

    @ApiModelProperty("标准名称 企业标准|行业标准")
    private String name;

    @ApiModelProperty("标准值 可能是Integer 可能是BigDecimal 还有可能是其余引用类型")
    private T value;

    @ApiModelProperty("有效期-开始时间")
    private LocalDate validStart;

    @ApiModelProperty("有效期-结束时间")
    private LocalDate validEnd;
}
