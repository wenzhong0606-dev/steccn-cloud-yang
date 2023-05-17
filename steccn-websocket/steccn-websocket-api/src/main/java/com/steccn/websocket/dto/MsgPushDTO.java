package com.steccn.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author yequan
 * @desc 消息推送机制
 */
@Data
@ApiModel("消息推送dto")
public class MsgPushDTO implements Serializable {

    @ApiModelProperty("推送类型")
    private Integer type;

    @ApiModelProperty("消息id")
    private Long msgId;

    @ApiModelProperty("消息类别")
    private Integer msgCategory;

    @ApiModelProperty("消息类型")
    private Integer msgType;


    /**
     * 强提醒标识 0:不提醒 1:提醒
     */
    @ApiModelProperty("强提醒标识")
    private Integer warnFlag;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("已读未读标识")
    private Integer readFlag;

    @ApiModelProperty("消息附加信息")
    private SysMsgAdditionDTO addition;

    @ApiModelProperty("消息创建人")
    private String createName;

    @ApiModelProperty("消息创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("用户id集合")
    private Set<Long> userIdList;
}
