package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 落板原因
 */
@Data
@ApiModel("落板原因")
public class QtCheckTaskDestoryOnlineVO implements Serializable {

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("落板原因")
    private String destoryReason;

    @ApiModelProperty("落板开始时间")
    private LocalDateTime destoryStartTime;

    @ApiModelProperty("落板结束时间")
    private LocalDateTime destoryEndTime;

    @ApiModelProperty("备注")
    private String destoryInfoRemark;

    @ApiModelProperty("第一个落板开始时间")
    private LocalDateTime firstTime;

    @ApiModelProperty("产线名称")
    private String productLineName;

    private static final long serialVersionUID = 1L;
}
