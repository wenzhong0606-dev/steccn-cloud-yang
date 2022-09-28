package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("第三方同步数据dto")
public class QtThirdPartyDefectiveDataDTO implements Serializable {

    @ApiModelProperty("异常代码，正常时为空")
    private String errcode;

    @ApiModelProperty("发送时间")
    private String product_time;

    @ApiModelProperty("线次")
    private String line;

    @ApiModelProperty("缺陷信息")
    private List<QtThirdPartyDefectContentDTO> defect_Content;


}
