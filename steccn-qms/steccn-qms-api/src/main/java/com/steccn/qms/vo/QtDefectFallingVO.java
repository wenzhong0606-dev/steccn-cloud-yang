package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 落板记录报表视图对象
 * @date 2023/7/11
 */
@Data
@ApiModel("落板记录报表视图对象")
public class QtDefectFallingVO implements Serializable {

    @ApiModelProperty("类型 0表示数据 1表示合计（单产线） 2表示总计")
    private Integer type;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次日期名称")
    private String classDateStr;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("明细-多行四列")
    private List<QtDefectFallingDetailVO> detailList;
}
