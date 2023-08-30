package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 产线加工信息分页列表
 * @date 2023/8/30
 */
@Data
@ApiModel("产线加工信息分页列表")
public class DashboardProducePageListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单主键")
    private Long orderId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("规格")
    private String specifications;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("计划数量")
    private BigDecimal planQuantity;

    @ApiModelProperty("完工数量")
    private BigDecimal completeQuantity;

    @ApiModelProperty("入库数量")
    private BigDecimal inStorageQuantity;

    @ApiModelProperty("欠额")
    private BigDecimal debtQuantity;

    @ApiModelProperty("计划开始时间")
    private LocalDateTime planStartTime;

    @ApiModelProperty("计划完成时间")
    private LocalDateTime planEndTime;

}
