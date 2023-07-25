package com.steccn.qms.vo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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

    @ApiModelProperty("数据来源 0手动  1人工")
    private Integer dataSource;

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

    @ApiModelProperty("班次日期")
    private LocalDate classesDate;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("缺陷记录开始时间")
    private LocalDateTime defectStartTime;

    @ApiModelProperty("缺陷记录结束时间")
    private LocalDateTime defectEndTime;

    @ApiModelProperty("自然小时开始时间")
    private LocalDateTime normalStartTime;

    @ApiModelProperty("自然小时结束时间")
    private LocalDateTime normalEndTime;

    @ApiModelProperty("完成时间")
    private LocalDateTime finishedTime;

    @ApiModelProperty("班次时间-基于这个时间来做的过滤数据集合")
    private LocalDate classTime;

    @ApiModelProperty("物料")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("客户主键")
    private Integer customerId;

    @ApiModelProperty("班组id")
    private Integer classGroupId;

    @ApiModelProperty("班组名称")
    private String classGroupName;


}
