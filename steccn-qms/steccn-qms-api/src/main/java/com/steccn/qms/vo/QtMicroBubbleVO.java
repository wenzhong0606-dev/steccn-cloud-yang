package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 微气泡报表视图对象
 * @date 2023/7/19
 */
@Data
@ApiModel("微气泡报表视图对象")
public class QtMicroBubbleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日期表头")
    private List<String> dateStrList;

    @ApiModelProperty("数据行")
    private List<QtMicroBubbleListVO> dataList;

}
