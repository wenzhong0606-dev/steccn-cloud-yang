package com.steccn.portal.dto.oa;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * oa checkLogin接口返回值
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@Data
@ApiModel("oa登录结果")
public class OaLoginResult {
    @ApiModelProperty("消息code")
    private int msgCode;
    @ApiModelProperty("消息")
    private String msg;
    @ApiModelProperty("登录状态true或false")
    private Boolean loginStatus;
    @ApiModelProperty("用户id")
    private int userId;
}
