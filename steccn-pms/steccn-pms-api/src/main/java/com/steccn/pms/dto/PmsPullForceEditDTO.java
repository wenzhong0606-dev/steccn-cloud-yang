package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/7 11:00
 */
@Data
public class PmsPullForceEditDTO {

    @ApiModelProperty("班次id")
    private Long classesId;

    private Long id;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("日期")
    private LocalDate pullForceDate;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("车间id")
    private Integer workshopId;
}
