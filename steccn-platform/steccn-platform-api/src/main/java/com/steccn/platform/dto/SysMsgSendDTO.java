package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 用户消息中心
 */
@Data
@ApiModel("用户消息中心")
public class SysMsgSendDTO implements Serializable {

    @ApiModelProperty("消息类型")
    @NotNull(message = "消息类型不能为空")
    private Integer type;

    @ApiModelProperty("标题")
    @NotBlank(message = "消息标题不能为空")
    private String title;

    @ApiModelProperty("消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @ApiModelProperty("备注内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SysMsgAdditionDTO addition;

    @ApiModelProperty("接收用户集合")
    @Size(min = 1, message = "消接收用户不能为空")
    @NotNull(message = "消接收用户不能为空")
    private Set<Long> userList;

    /**
     * @see com.steccn.websocket.constant.WebsocketConstant.WsPushTypeEnum
     */
    @ApiModelProperty("推送类型")
    @NotNull(message = "推送类型不能为空")
    private Integer pushType;
}
