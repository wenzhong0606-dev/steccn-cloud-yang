package com.steccn.websocket.dto;

import com.steccn.platform.vo.SysMsgAdditionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 消息推送机制
 */
@Data
@ApiModel("消息推送dto")
public class MsgPushDTO implements Serializable {

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("消息id")
    private Long msgId;

    @ApiModelProperty("消息类别")
    private Integer msgCategory;

    @ApiModelProperty("消息类型")
    private Integer msgType;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("消息")
    private SysMsgAdditionDTO addition;

    @ApiModelProperty("用户id集合")
    private Set<Long> userIdList;
}
