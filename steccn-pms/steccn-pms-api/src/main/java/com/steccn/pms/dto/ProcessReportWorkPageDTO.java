package com.steccn.pms.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 工序报工分页dto
 */
@Data
@ApiModel("工序报工分页dto")
public class ProcessReportWorkPageDTO extends BasePageDTO {

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("生产订单编码")
    private String produceOrderNo;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("工位")
    private Integer stationId;

    @ApiModelProperty("工序")
    private Long processId;

    @ApiModelProperty("班次")
    private Long classesId;

    @ApiModelProperty("创建时间-开始")
    private LocalDate createDtStart;

    @ApiModelProperty("创建时间-结束")
    private LocalDate createDtEnd;

    @ApiModelProperty("开始时间-开始")
    private LocalDate startDtStart;

    @ApiModelProperty("开始时间-结束")
    private LocalDate startDtEnd;

    @ApiModelProperty("结束时间-开始")
    private LocalDate endDtStart;

    @ApiModelProperty("结束时间-结束")
    private LocalDate endDtEnd;

    @ApiModelProperty(value = "创建时间-开始", hidden = true)
    private LocalDateTime createTimeStart;

    @ApiModelProperty(value = "创建时间-结束", hidden = true)
    private LocalDateTime createTimeEnd;

    @ApiModelProperty(value = "开始时间-开始", hidden = true)
    private LocalDateTime startTimeStart;

    @ApiModelProperty(value = "开始时间-结束", hidden = true)
    private LocalDateTime startTimeEnd;

    @ApiModelProperty(value = "结束时间-开始", hidden = true)
    private LocalDateTime endTimeStart;

    @ApiModelProperty(value = "结束时间-结束", hidden = true)
    private LocalDateTime endTimeEnd;

    @ApiModelProperty("产线")
    private Integer productionLineId;

    @ApiModelProperty("班组")
    private Long teamId;

    @ApiModelProperty("产线id")
    private List<Integer> productLineIds;
}
