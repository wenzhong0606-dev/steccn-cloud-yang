package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    private Long produceOrderId;

    @ApiModelProperty("工位")
    private Integer stationId;

    @ApiModelProperty("工序主键")
    private Integer processId;

    @ApiModelProperty("加工数量")
    private BigDecimal processQuantity;
}
