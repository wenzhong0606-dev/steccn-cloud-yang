package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产订单批次VO
 *
 * @author Mongknife
 * @date 2022/10/11
 */
@Data
@ApiModel("生产订单批次信息VO")
public class PmsWorkOrderBatchVO {

    @ApiModelProperty("主键")
    private Long  id;

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

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("是否当前批次")
    private Integer currentBatchFlag;

    @ApiModelProperty("最小包装数量")
    private Integer minPackQuantity;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("生产订单code")
    private String produceOrderCode;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("物料id")
    private Long materialId;

}
