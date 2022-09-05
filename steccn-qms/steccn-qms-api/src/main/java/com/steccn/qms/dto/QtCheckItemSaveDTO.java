package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 新建检验项dto
 */
@Data
@ApiModel("新建检验项dto")
public class QtCheckItemSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("检验项名称")
    private String name;

    @ApiModelProperty("数据类型")
    private Integer dataType;

    @ApiModelProperty("计量单位")
    private Integer measureUnit;

    @ApiModelProperty("可能值")
    private String possibleValue;

    @ApiModelProperty("默认值")
    private String defaultValue;

    @ApiModelProperty("检验项类别id")
    private Integer categoryId;

    @ApiModelProperty("操作说明")
    private String opDesc;

    @ApiModelProperty("备注")
    private String note;
}
