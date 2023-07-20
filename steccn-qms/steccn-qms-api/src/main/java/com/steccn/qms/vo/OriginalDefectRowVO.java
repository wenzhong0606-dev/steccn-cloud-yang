package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 原片不良报表行数据视图对象
 * @date 2023/7/19
 */
@Data
@ApiModel("原片不良报表行数据视图对象")
public class OriginalDefectRowVO implements Serializable {

    @ApiModelProperty("类型 0数据 1合计")
    private Integer type;

    @ApiModelProperty("产线")
    private String productLineName;

    @ApiModelProperty("日期数据")
    private List<String> dateDataList;

    @ApiModelProperty("合计")
    private String sum;
}
