package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 物料单位转换
 */
@Data
@ApiModel("物料单位转换")
public class SysSpareUnitConversionVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("备件id")
    private Long spareId;

    @ApiModelProperty("标准数量")
    private BigDecimal quantity;

    @ApiModelProperty("标准单位")
    private Long unitId;

    @ApiModelProperty("换算数量")
    private BigDecimal conversionQunantity;

    @ApiModelProperty("换算单位")
    private Long conversionUintId;

    private static final long serialVersionUID = 1L;
}
