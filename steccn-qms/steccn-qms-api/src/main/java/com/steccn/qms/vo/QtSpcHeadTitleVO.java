package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yequan
 * @desc 动态表头
 */
@Data
@ApiModel("动态表头")
@NoArgsConstructor
@AllArgsConstructor
public class QtSpcHeadTitleVO {

    @ApiModelProperty("动态表格头的value")
    private String value;

    @ApiModelProperty("标签")
    private String label;
}
