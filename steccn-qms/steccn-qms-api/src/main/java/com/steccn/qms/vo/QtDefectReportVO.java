package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 不良报表
 * @date 2023/7/11
 */
@Data
@ApiModel("不良报表视图对象")
public class QtDefectReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("数据集合")
    private List<QtDefectReportDataVO> dataList;

    @ApiModelProperty("动态表头")
    private List<String> valueHeadList;


}
