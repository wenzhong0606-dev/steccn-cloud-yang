package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 检验项和缺陷项类型表
 */
@Data
@ApiModel("检验项和缺陷项类型信息")
public class QtItemCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("类别 0检验项 1缺陷项")
    private Integer type;

    @ApiModelProperty("名称")
    private String name;

}
