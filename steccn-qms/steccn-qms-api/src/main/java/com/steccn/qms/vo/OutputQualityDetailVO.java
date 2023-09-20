package com.steccn.qms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("产量质量缺陷项明细VO")
public class OutputQualityDetailVO {

    @ApiModelProperty("检验任务id")
    private Long taskId;

    @ApiModelProperty("缺陷项id")
    private Integer defectId;

    @ApiModelProperty("缺陷项名称")
    private String defectName;

    @ApiModelProperty("缺陷项数量")
    private Integer defectNum;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("生产车间id")
    private Integer productWorkshopId;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("班组")
    private Integer classGroupId;

    @ApiModelProperty("班组名称")
    private String  classGroupName;

    @ApiModelProperty("生产订单编码")
    private String productOrder;

    @ApiModelProperty("检验项id")
    private Integer checkItemId;

    @ApiModelProperty("检验结果")
    private String checkResult;

    @ApiModelProperty("缺陷类别")
    private Integer defectCategory;

    @ApiModelProperty("缺陷类别名称")
    private String defectCategoryName;


}
