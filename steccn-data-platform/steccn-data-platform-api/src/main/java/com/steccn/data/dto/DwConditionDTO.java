package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓条件dto
 */
@Data
@ApiModel("数仓条件dto")
public class DwConditionDTO implements Serializable {

    @ApiModelProperty("仪表id")
    private Integer meterId;

    @ApiModelProperty("抄表日期")
    private LocalDate readDate;

    @ApiModelProperty("班次id 0早班 1晚班")
    private Integer classId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;

    @ApiModelProperty("生产订单主键")
    private Long produceOrderId;
}
