package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工缺陷类别分布-明细
 */
@Data
@ApiModel("深加工缺陷类别分布-明细")
public class DeepDefectiveCategoryDetailVO implements Serializable {

    @ApiModelProperty("缺陷项&缺陷类别 id")
    private Integer id;

    @ApiModelProperty("缺陷项&缺陷类别 id")
    private String name;

    @ApiModelProperty("缺陷项&缺陷类别 创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("缺陷项数量")
    private Integer defectNum;

    @ApiModelProperty("生产车间id")
    private Integer productWorkshopId;

    @ApiModelProperty("生产车间名称")
    private String productWorkshopName;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("产线id")
    private String productLineName;

    @ApiModelProperty("班次日期")
    private LocalDate classesDate;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("班次名字")
    private String classesName;

    @ApiModelProperty("明细list")
    private List<DeepDefectiveCategoryDetailListVO> detailList;

}
