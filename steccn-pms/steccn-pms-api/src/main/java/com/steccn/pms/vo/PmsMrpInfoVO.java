package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mrp详情DTO
 *
 * @author Mongknife
 * @date 2022/11/8
 */
@Data
@ApiModel("Mrp详情DTO")
public class PmsMrpInfoVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("mrp时间")
    private LocalDate mrpDate;

    @ApiModelProperty("mrp类型id")
    private Long mrpTypeId;

    @ApiModelProperty("mrp类型名称")
    private Long mrpTypeName;

    @ApiModelProperty("mrp元素")
    private String mrpElement;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("库存可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("计划id")
    private Long planId;

    @ApiModelProperty("状态")
    private Integer status;
}

