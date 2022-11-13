package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ApiModelProperty("缺陷项类别创建时间")
    private LocalDateTime defectCategoryCreateTime;

    @ApiModelProperty("缺陷项数量")
    private Integer defectNum;

    @ApiModelProperty("生产车间id")
    private Integer productWorkshopId;

    @ApiModelProperty("生产车间名字")
    private String productWorkshopName;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("班次日期")
    private LocalDate classesDate;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("缺陷记录开始时间")
    private LocalDateTime defectStartTime;

    @ApiModelProperty("缺陷记录结束时间")
    private LocalDateTime defectEndTime;

}
