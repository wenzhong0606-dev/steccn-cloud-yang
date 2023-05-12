package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓生产订单的数据
 * @date 2023/5/11
 */
@Data
@ApiModel("数仓生产订单的数据")
public class DwProduceOrderDTO implements Serializable {

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("报工开始时间")
    private LocalDateTime start;

    @ApiModelProperty("报工结束时间")
    private LocalDateTime end;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;
}
