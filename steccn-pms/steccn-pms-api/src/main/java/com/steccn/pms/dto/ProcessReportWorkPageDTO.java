package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel("工序报工分页dto")
public class ProcessReportWorkPageDTO extends BasePageDTO {

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("生产订单编码")
    private String produceOrderNo;

    @ApiModelProperty("工位")
    private Integer stationId;

    @ApiModelProperty("工序")
    private Long processId;

    @ApiModelProperty("班次")
    private Integer classesId;

    @ApiModelProperty("创建时间-开始")
    private LocalDate createTimeStart;

    @ApiModelProperty("创建时间-结束")
    private LocalDate createTimeEnd;

    @ApiModelProperty("开始时间-开始")
    private LocalDate startTimeStart;

    @ApiModelProperty("开始时间-结束")
    private LocalDate startTimeEnd;

    @ApiModelProperty("结束时间-开始")
    private LocalDate endTimeStart;

    @ApiModelProperty("结束时间-结束")
    private LocalDate endTimeEnd;
}
