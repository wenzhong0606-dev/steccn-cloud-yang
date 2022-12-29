package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 当前班次日期下的报工数据
 */
@Data
@ApiModel("当前班次日期下的报工数据")
public class DwCurrentReportWorkDTO implements Serializable {

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("部门id")
    private Integer departmentId;

    @ApiModelProperty("车间id")
    private Integer workshopId;

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("工位")
    private Integer stationId;

    @ApiModelProperty("报工开始时间")
    private LocalDateTime start;

    @ApiModelProperty("报工结束时间")
    private LocalDateTime end;

    @ApiModelProperty("报工id")
    private Long reportWorkId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

}
