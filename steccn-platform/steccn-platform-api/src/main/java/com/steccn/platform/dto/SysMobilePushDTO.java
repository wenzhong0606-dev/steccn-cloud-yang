package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 移动端消息推送dto
 */
@Data
@ApiModel("移动端消息推送dto")
public class SysMobilePushDTO implements Serializable {

    @ApiModelProperty("设备id，支持多个以数组的形式指定多个设备，如[\"cid-1\",\"cid-2\"]，数组长度不大于1000")
    private String push_clientid;

    @ApiModelProperty("消息分类")
    private Integer category;

    @ApiModelProperty("消息类型")
    private Integer type;

    @ApiModelProperty("标题")
    private String title;


    /**
     * 强提醒标识 0:不提醒 1:提醒
     */
    @ApiModelProperty("强提醒标识")
    private Integer warnFlag;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("数据")
    private String payload;

    @ApiModelProperty("服务端推送 需要加这一句")
    private boolean force_notification;

    @ApiModelProperty("请求唯一标识号，10-32位之间；如果request_id重复，会导致消息丢失")
    private String request_id;

    @ApiModelProperty("消息分类，没申请可以不传这个参数")
    private String options;

    @ApiModelProperty("用户id")
    private Long userId;
}
