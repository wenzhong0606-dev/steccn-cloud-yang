package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-在线检
 * @date 2023/6/12
 */
@Data
@ApiModel("质量日报表-在线检")
public class QtDayReportOnlineDTO implements Serializable {

    @ApiModelProperty("班次日期")
    private LocalDate date;

    @ApiModelProperty("产线id集合")
    private List<Integer> productLineIdList;
}
