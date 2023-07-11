package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 落板记录数据传输对象
 * @date 2023/7/11
 */
@Data
@ApiModel("落板记录数据传输对象")
public class QtDestoryInfoFallDTO implements Serializable {

    @ApiModelProperty("检验类型")
    private Integer taskType;

    @ApiModelProperty("产线")
    private Integer productLine;

    @ApiModelProperty("班次日期")
    private LocalDateTime classTime;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("班组")
    private Integer classGroupId;

    @ApiModelProperty("落板开始时间")
    private LocalDateTime destoryStartTime;

    @ApiModelProperty("落板结束时间")
    private LocalDateTime destoryEndTime;

    @ApiModelProperty("落板原因")
    private String destoryReason;

    @ApiModelProperty("落板工位多选框 1南 2北 3全选")
    private Integer destoryWorkStation;
}
