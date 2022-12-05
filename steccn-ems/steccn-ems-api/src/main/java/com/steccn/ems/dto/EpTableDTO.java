package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 能耗图表dto
 */
@ApiModel("能耗图表dto")
@Data
public class EpTableDTO implements Serializable {

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("时间-天、周、月、无粒度")
    private Integer granularity;

}
