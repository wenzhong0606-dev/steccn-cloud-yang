package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel("深加工或基片合格率DTO")
public class QtSubOrDeepAcceptabilityDTO implements Serializable {

    @ApiModelProperty("深加工或者基片")
    private String name;

    @ApiModelProperty("开始时间")
    @NotNull(message = "开始时间不能为空")
    private LocalDate dateStart;

    @ApiModelProperty("结束时间")
    @NotNull(message = "结束时间不能为空")
    private LocalDate dateEnd;

    @ApiModelProperty("对比")
    private Integer compare;

    @ApiModelProperty("生产车间")
    private Integer productWorkShopId;

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("班次")
    private Integer workingOrder;

    @ApiModelProperty("物料id")
    private Integer materialId;

    @ApiModelProperty("客户")
    private Integer customer;

    @ApiModelProperty("类型:0柱状图,1饼状图")
    private Integer type;
}
