package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 报工多工序DTO
 *
 * @author AndrewLuo
 * @date 2025/04/14
 */
@Data
public class ProcessReportWorkSubmitProcessDTO {
    @NotNull(message = "工序不能为空")
    private Long id;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("加工数量")
//    @NotNull(message = "加工数量不能为空")
//    @DecimalMin(value="0",message="加工数量应大于0")
//    @DecimalMax(value="99999999.999",message="加工数量大小应小于99999999.999")
//    @Digits(integer = 8,fraction = 3,message = "加工数量整数位上限为8位，小数位上限位3位")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数量")
//    @NotNull(message = "不良数量不能为空")
//    @DecimalMin(value="0",message="不良数量应大于0")
//    @DecimalMax(value="99999999.999",message="不良数量大小应小于99999999.999")
//    @Digits(integer = 8,fraction = 3,message = "不良不良数量整数位上限为8位，小数位上限位3位")
    private BigDecimal badnessQuantity;
}
