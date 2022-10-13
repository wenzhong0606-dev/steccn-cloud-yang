package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 检验项回显Vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanItemVO extends QtCheckItemListVO implements Serializable{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("检验项id")
    private Integer itemId;

    @ApiModelProperty("极差数组")
    List<QtRangeArrayVO> rangeArray;

    @ApiModelProperty("平均数数组")
    List<QtAverageArrayVO> averageArray;

    @ApiModelProperty("方差数组")
    List<QtMeanArrayVO> meanArray;

    @ApiModelProperty("截尾平均数数组")
    List<QtEndAverageArrayVO> endAverageArray;

    @ApiModelProperty("标准值")
    private BigDecimal standardValue;

    @ApiModelProperty("上偏差")
    private BigDecimal upperValue;

    @ApiModelProperty("下偏差")
    private BigDecimal lowerValue;

    @ApiModelProperty("排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty("关联缺陷项")
    private List<QtLinkedDefectiveItemVO> linkedDefectiveItem;

    @ApiModelProperty("关联客户")
    private List<QtLinkedCustomerCodeVO> linkedCustomerCode;

    @ApiModelProperty("检验项结果")
    private String checkResult;

    @ApiModelProperty("缺陷项id")
    private Integer defectId;

    @ApiModelProperty("缺陷项名字")
    private String defectName;

    /**
     * pc端任务详情专用
     */
    private Map<String,String> result;
}
