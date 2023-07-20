package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 原片不良报表视图对象
 * @date 2023/7/19
 */
@Data
@ApiModel("原片不良报表视图对象")
public class OriginalDefectVO implements Serializable {

    @ApiModelProperty("日期格式")
    private List<String> dateStrList;

    @ApiModelProperty("行数据集合")
    private List<OriginalDefectRowVO> rowList;

}
