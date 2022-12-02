package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 图表单耗预警vo
 */
@Data
@ApiModel("单耗预警vo")
public class EpChartUnitConsumeAlarmVO implements Serializable {

    @ApiModelProperty("能源类型")
    private Integer energyType;

    @ApiModelProperty("组织机构id")
    private Integer officeId;

    @ApiModelProperty("组织机构name")
    private String officeName;

    @ApiModelProperty("预警时间")
    private LocalDateTime alarmDateTime;

    @ApiModelProperty("0行业标准 1公司标准 2部门标准")
    private Integer standardType;

    @ApiModelProperty("时间")
    private LocalDate conditionDate;

    @ApiModelProperty("当前值")
    private BigDecimal currentValue;

    @ApiModelProperty("标准值")
    private BigDecimal standardValue;

}
