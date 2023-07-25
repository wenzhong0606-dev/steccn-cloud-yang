package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 镀膜不良月报表视图对象
 * @date 2023/7/20
 */
@Data
@ApiModel("镀膜不良月报表视图对象")
public class CoatingFilmVO implements Serializable {

    @ApiModelProperty("表头")
    private List<String> headerList;

    @ApiModelProperty("数据行集合")
    private List<CoatingFilmRowVO> rowList;

}
