package com.steccn.qms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/06
 */
@Data
public class QtCheckPlanItemsDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("检验项id")
    private Integer itemId;

    @ApiModelProperty("极差数组")
    List<Integer> rangeArray;

    @ApiModelProperty("平均数数组")
    List<Integer> averageArray;

    @ApiModelProperty("标准值")
    private Double standardValue;

    @ApiModelProperty("上偏差")
    private Double upperValue;

    @ApiModelProperty("下偏差")
    private Double lowerValue;

    @ApiModelProperty("关联缺陷项")
    private List<Integer> linkedDefectiveItem;

    @ApiModelProperty("关联客户")
    private List<String> linkedCustomerCode;
}
