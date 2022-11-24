package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 报工能耗
 */
@ApiModel("报工能耗")
@Data
public class ProcessReportWorkConsumeDTO implements Serializable {

    @ApiModelProperty("类型")
    @NotNull(message = "类型不能为空")
    private Integer type;

    @ApiModelProperty("能耗数量")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value="0",message="数量应大于0")
    @DecimalMax(value="99999999.999",message="数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    @NotNull(message = "能耗单位不能为空")
    private Long unitId;

}
