package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author mengle
 * @desc 缺陷项
 */
@Data
@ApiModel("缺陷项")
public class QtCheckTaskDefectItemVO implements Serializable {

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("产线名称")
    private String productLineName;

    private static final long serialVersionUID = 1L;
}
