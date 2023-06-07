package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("产量质量明细DTO")
public class OutputQualityDetailDTO {

    @ApiModelProperty("班次开始时间")
    private LocalDate startDate;


    @ApiModelProperty("班次结束时间")
    private LocalDate endDate;


    @ApiModelProperty("生产车间id")
    private List<Integer> productWorkshopId;


}
