package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel("深加工或基片合格率")
public class QtSubOrDeepAcceptabilityDTO implements Serializable {

    @ApiModelProperty("开始时间")
    private LocalDate dateStart;

    @ApiModelProperty("结束时间")
    private LocalDate dateEnd;

    @ApiModelProperty("对比")
    private String compare;

    @ApiModelProperty("生产车间")
    private QtBaseIdNameDTO productWorkshop;

    @ApiModelProperty("产线")
    private QtBaseIdNameDTO productLine;

    @ApiModelProperty("班次")
    private String workingOrder;

    @ApiModelProperty("物料id")
    private Integer materialId;

    @ApiModelProperty("客户")
    private QtBaseIdNameDTO customer;


}
