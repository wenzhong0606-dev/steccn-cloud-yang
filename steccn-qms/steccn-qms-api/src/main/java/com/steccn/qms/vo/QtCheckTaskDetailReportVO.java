package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 检验任务明细信息报表vo
 */
@ApiModel("检验任务明细信息报表vo")
@Data
public class QtCheckTaskDetailReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("任务code")
    private String taskCode;

    @ApiModelProperty("缺陷项id")
    private Integer defectId;

    @ApiModelProperty("缺陷项类别")
    private Integer defectCategory;

    @ApiModelProperty("缺陷项数量")
    private Integer defectNum;

    @ApiModelProperty("生产车间id")
    private Integer productWorkshopId;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("班次")
    private Integer classId;
}
