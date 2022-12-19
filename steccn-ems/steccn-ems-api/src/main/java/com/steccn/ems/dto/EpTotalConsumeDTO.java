package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @desc 总体能耗趋势（近7日） 请求dto
 * @author yequan
 */
@ApiModel("总体能耗趋势（近7日） 请求dto")
@Data
public class EpTotalConsumeDTO implements Serializable {

    @ApiModelProperty(value = "能源类型id" , hidden = true)
    private List<Integer> energyType;

    @ApiModelProperty("开始日期")
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    @ApiModelProperty("结束日期")
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    @ApiModelProperty("时间-天、周、月、无粒度")
    private Integer granularity;
}
