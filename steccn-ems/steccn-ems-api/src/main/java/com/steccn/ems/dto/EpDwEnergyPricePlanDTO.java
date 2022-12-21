package com.steccn.ems.dto;

import com.steccn.common.util.BigDecimalUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author yequan
 * @desc 数仓-能源价格方案
 */
@ApiModel("数仓-能源价格方案")
@Data
public class EpDwEnergyPricePlanDTO implements Serializable {


    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("节点id")
    private Long partitionId;

    @ApiModelProperty("远传仪表编码")
    private String distantMapCode;

    @ApiModelProperty("开始时间")
    private LocalTime start;

    @ApiModelProperty("结束时间")
    private LocalTime end;

    @ApiModelProperty("峰0 谷1 平2")
    private Integer type;

    @ApiModelProperty("价格")
    private BigDecimal price;

}
