package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 消息附加信息
 */
@Data
@ApiModel("消息附加信息")
public class SysMsgAdditionDTO implements Serializable {

    @ApiModelProperty("附加信息-主键")
    private Long id;

    @ApiModelProperty("附加信息-tab")
    private Long tab;

    @ApiModelProperty("附加信息-编码")
    private String code;
}
