package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author yequan
 */
@Data
@ApiModel("报工订单列表查询dto")
public class ReportWorkOrderPageDTO extends BasePageDTO {

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("创建时间-开始")
    private LocalDate createDtStart;

    @ApiModelProperty("创建时间-结束")
    private LocalDate createDtEnd;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("计划开始时间-开始")
    private LocalDate psDtStart;

    @ApiModelProperty("计划开始时间-结束")
    private LocalDate psDtEnd;

    @ApiModelProperty("计划结束时间-开始")
    private LocalDate peDtStart;

    @ApiModelProperty("计划结束时间-结束")
    private LocalDate peDtEnd;
}
