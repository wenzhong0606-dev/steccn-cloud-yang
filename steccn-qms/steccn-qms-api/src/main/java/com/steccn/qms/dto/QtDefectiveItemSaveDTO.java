package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @desc 缺陷项保存dto
 */
@Data
@ApiModel("缺陷项保存dto")
public class QtDefectiveItemSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("缺陷项名称")
    private String name;

    @ApiModelProperty("缺陷项备注")
    private String note;

    @ApiModelProperty("检验项类别id")
    private Integer categoryId;
}
