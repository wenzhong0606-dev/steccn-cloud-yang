package com.steccn.pms.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel("产量质量报表")
public class OutputQualityDTO {


    @ApiModelProperty("日期")
    private LocalDate date;

    @ApiModelProperty("车间id")
    private List<Integer> productWorkshop;
 }
