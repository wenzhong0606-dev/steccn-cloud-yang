package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产订单分页Dto
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@ApiModel("生产订单分页Dto")
@Data
public class PmsProduceOrderPageDTO  extends BasePageDTO {

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("生产订单编码")
    private String code;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("产线id")
    private Integer productionLineId;

    @ApiModelProperty("订单类型")
    private Long typeId;

    @ApiModelProperty("计划开始日期-开始")
    private LocalDate planStartDateStart;

    @ApiModelProperty("计划开始日期-结束")
    private LocalDate planStartDateEnd;

    @ApiModelProperty("计划结束日期-开始")
    private LocalDate planEndDateStart;

    @ApiModelProperty("计划结束日期-结束")
    private LocalDate planEndDateEnd;

    @ApiModelProperty("实际开始日期-开始")
    private LocalDate actualStartDateStart;

    @ApiModelProperty("实际开始日期-结束")
    private LocalDate actualStartDateEnd;

    @ApiModelProperty("实际结束日期-开始")
    private LocalDate actualEndDateStart;

    @ApiModelProperty("实际结束日期-结束")
    private LocalDate actualEndDateEnd;

    @ApiModelProperty("计划开始时间-开始")
    private LocalDateTime planStartTimeStart;

    @ApiModelProperty("计划开始时间-结束")
    private LocalDateTime planStartTimeEnd;

    @ApiModelProperty("计划结束时间-开始")
    private LocalDateTime planEndTimeStart;

    @ApiModelProperty("计划结束时间-结束")
    private LocalDateTime planEndTimeEnd;

    @ApiModelProperty("实际开始时间-开始")
    private LocalDateTime actualStartTimeStart;

    @ApiModelProperty("实际开始时间-结束")
    private LocalDateTime actualStartTimeEnd;

    @ApiModelProperty("实际结束时间-开始")
    private LocalDateTime actualEndTimeStart;

    @ApiModelProperty("实际结束时间-结束")
    private LocalDateTime actualEndTimeEnd;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("开始日期")
    private LocalDate startDate;

    @ApiModelProperty("结束日期")
    private LocalDate endDate;
}
