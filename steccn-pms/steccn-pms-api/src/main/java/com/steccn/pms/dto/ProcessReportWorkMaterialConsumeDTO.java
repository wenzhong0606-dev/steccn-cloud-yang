package com.steccn.pms.dto;

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
 * @desc 工单报工，物料消耗
 */
@Data
public class ProcessReportWorkMaterialConsumeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单主键")
    @NotNull(message = "生产订单主键不能为空")
    private Long produceOrderId;

    @ApiModelProperty("工位")
    @NotNull(message = "工位不能为空")
    private Integer stationId;

    @ApiModelProperty("工序主键")
    @NotNull(message = "工序不能为空")
    private Integer processId;

    @ApiModelProperty("加工数量")
    @NotNull(message = "加工数量不能为空")
    @DecimalMin(value="0",message="加工数量应大于0")
    @DecimalMax(value="99999999.999",message="加工数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "加工数量整数位上限为8位，小数位上限位3位")
    private BigDecimal processQuantity;
}
