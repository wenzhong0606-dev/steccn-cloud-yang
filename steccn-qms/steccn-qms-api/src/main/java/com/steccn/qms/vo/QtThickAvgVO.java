package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 平均厚度集合视图对象
 * @date 2023/7/12
 */
@Data
@ApiModel("平均厚度集合视图对象")
public class QtThickAvgVO implements Serializable {

    @ApiModelProperty("表头日期集合")
    private List<String> dateList;

    @ApiModelProperty("数据集合")
    private List<QtThickAvgListVO> rowList;
}
