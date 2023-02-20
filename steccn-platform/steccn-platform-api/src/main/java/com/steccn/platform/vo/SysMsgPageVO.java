package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 消息中心分页vo
 */
@Data
@ApiModel("消息中心分页vo")
public class SysMsgPageVO implements Serializable {

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("消息ID")
    private Long msgId;

    @ApiModelProperty("消息标题")
    private String title;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("已读未读")
    private Integer readFlag;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("消息发起人")
    private String createName;
}
