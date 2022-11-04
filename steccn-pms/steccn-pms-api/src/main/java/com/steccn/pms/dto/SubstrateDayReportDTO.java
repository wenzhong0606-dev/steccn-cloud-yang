package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @desc 基片日报表
 * @author yequan
 */
@ApiModel("基片日报表")
@Data
public class SubstrateDayReportDTO implements Serializable {

    @ApiModelProperty("车间")
    private List<Integer> workshopIdList;

    @ApiModelProperty("产线")
    private List<Integer> productLineIdList;

    @ApiModelProperty("班组")
    private List<Long> classGroupIdList;

    @ApiModelProperty("日期")
    private LocalDate date;
}
