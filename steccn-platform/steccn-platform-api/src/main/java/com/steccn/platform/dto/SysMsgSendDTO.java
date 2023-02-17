package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @desc 用户消息中心
 * @author yequan
 */
@Data
@ApiModel("用户消息中心")
public class SysMsgSendDTO implements Serializable {

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("接收用户集合")
    private Set<Long> userList;
}
