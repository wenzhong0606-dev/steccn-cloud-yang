package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 微气泡数据行
 * @date 2023/7/19
 */
@Data
@ApiModel("微气泡数据行")
public class QtMicroBubbleListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("线别")
    private String lineSite;

    @ApiModelProperty("日期数据")
    private List<List<String>> dateDataList;

}
