package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单批次DTO
 *
 * @author Mongknife
 * @date 2022/10/14
 */
@Data
@ApiModel("订单批次DTO")
public class ProduceOrderBatchDto {

    @ApiModelProperty("批次号")
    private String code;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("是否当前批次")
    private Integer currentBatchFlag;

    @ApiModelProperty("最小包装数量")
    private Integer minPackQuantity;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;
}
