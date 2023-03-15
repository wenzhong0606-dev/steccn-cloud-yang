package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 投诉类型
 * @date 2023/3/15
 */
@ApiModel("投诉类型")
@Data
public class QtComplainTypeVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("投诉类型名称")
    private String type;

    @ApiModelProperty("备注")
    private String note;

    private static final long serialVersionUID = 1L;
}
