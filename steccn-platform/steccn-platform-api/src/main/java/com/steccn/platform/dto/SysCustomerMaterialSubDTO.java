package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 客户物料明细dto
 * @date 2023/3/3
 */
@ApiModel("客户物料明细dto")
@Data
public class SysCustomerMaterialSubDTO implements Serializable {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料规格")
    private String materialSpec;

    @ApiModelProperty("是否固定用量")
    private Integer fixedQuantityFlag;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("数量单位")
    private Long unit;

    @ApiModelProperty("数量单位")
    private String unitName;

    @ApiModelProperty("是否回收 0否 1是")
    private Integer recycleFlag;

}
