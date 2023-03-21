package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 工序报工-加工时间
 */
@Data
@ApiModel("工序报工-加工时间")
public class ProcessReportWorkHourDTO implements Serializable {

    @ApiModelProperty("原因（字典：工时原因）")
    @NotNull(message = "原因不能为空")
    private Long causeId;

    @ApiModelProperty("工时")
    @NotNull(message = "工时不能为空")
    @DecimalMin(value="0",message="工时应大于0")
    @DecimalMax(value="720",message="工时应小于720")
    @Digits(integer = 3,fraction = 2,message = "工时整数位上限为3位，小数位上限位2位")
    private BigDecimal manHour;


    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @ApiModelProperty("工时单位（字典时间单位）")
    @NotNull(message = "工时单位不能为空")
    private Long unitId;


    /**
     * 备注
     */
    @Length(max = 100, message = "备注不能超过100个文字")
    private String note;

}
