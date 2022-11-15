package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 */

@ApiModel("缺陷项报表listvo")
@Data
public class DeepDefectiveCategoryDetailListVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("任务code")
    private String taskCode;

    @ApiModelProperty("检验结果来源")
    private String resultSource;

    @ApiModelProperty("缺陷项id")
    private Integer defectId;

    @ApiModelProperty("缺陷项名称")
    private String defectName;

    @ApiModelProperty("缺陷项类别")
    private Integer defectCategory;

    @ApiModelProperty("缺陷项类别")
    private String defectCategoryName;

    @ApiModelProperty("缺陷项数量")
    private Integer defectNum;

    @ApiModelProperty("缺陷记录开始时间")
    private LocalDateTime defectStartTime;

    @ApiModelProperty("缺陷记录结束时间")
    private LocalDateTime defectEndTime;

}

