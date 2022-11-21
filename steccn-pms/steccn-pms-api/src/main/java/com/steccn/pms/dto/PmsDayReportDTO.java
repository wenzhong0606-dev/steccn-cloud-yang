package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 基片日报表
 */
@ApiModel("日报表筛选条件")
@Data
public class PmsDayReportDTO implements Serializable {

    @ApiModelProperty("产线")
    private List<Integer> productLineIdList;

    @ApiModelProperty("班次")
    private List<Long> classesIdList;

    @ApiModelProperty("日期")
    private LocalDate date;
}
