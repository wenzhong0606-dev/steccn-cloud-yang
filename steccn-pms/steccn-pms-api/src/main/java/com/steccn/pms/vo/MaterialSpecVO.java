package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 物料规格vo
 */
@ApiModel("物料规格")
@Data
public class MaterialSpecVO implements Serializable {

    @ApiModelProperty("规格-长度")
    private BigDecimal length;

    @ApiModelProperty("规格-宽度")
    private BigDecimal width;

    @ApiModelProperty("规格-高度")
    private BigDecimal height;
}
