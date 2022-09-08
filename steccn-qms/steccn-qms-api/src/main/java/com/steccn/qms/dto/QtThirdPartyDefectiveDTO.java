package com.steccn.qms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 第三方系统同步缺陷dto
 */
@Data
@ApiModel("第三方系统同步缺陷dto")
public class QtThirdPartyDefectiveDTO implements Serializable {

    @ApiModelProperty("消息类型")
    @JsonProperty(value = "MsgEchoType")
    private String msgEchoType;

    @ApiModelProperty("保留字段")
    private String reserve01;

    @ApiModelProperty("数据")
    private QtThirdPartyDefectiveDataDTO data;

}
