package com.steccn.pms.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("生产详情")
public class PmsProducerOrderInfoDTO {


    /**
     * 生产批次号
     */
    @ApiModelProperty("生产批次号")
    private String batchNo;

    /**
     * 生产订单id
     */
    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    /**
     * 生产订单号
     */
    @ApiModelProperty("生产订单号")
    private String produceOrderCode;

    /**
     * 物料名称
     */
    @ApiModelProperty("物料名称")
    private String  materialName;
}
