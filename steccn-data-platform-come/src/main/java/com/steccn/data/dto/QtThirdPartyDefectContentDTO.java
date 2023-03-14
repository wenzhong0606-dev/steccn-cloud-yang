package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 第三方系统同步内容dto
 */
@Data
@ApiModel("第三方系统同步内容dto")
public class QtThirdPartyDefectContentDTO implements Serializable {

    @ApiModelProperty("缺陷种类")
    private String defect_type;

    @ApiModelProperty("缺陷等级")
    private String level;

    @ApiModelProperty("缺陷数量")
    private String defect_count;

}
