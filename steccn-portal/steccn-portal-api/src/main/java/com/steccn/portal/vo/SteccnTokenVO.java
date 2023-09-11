package com.steccn.portal.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("凯盛token")
public class SteccnTokenVO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户名")
    private String usercode;

    @ApiModelProperty("accessToken")
    private String accessToken;

    @ApiModelProperty("tokenType")
    private String tokenType;

    @ApiModelProperty("refreshToken")
    private String refreshToken;

    @ApiModelProperty("expiresIn")
    private Long expiresIn;

    @ApiModelProperty("scope")
    private String scope;

    @ApiModelProperty("oa系统的token")
    private String oaToken;

    @ApiModelProperty("oa系统的用户id")
    private Integer oaUserId;

    @ApiModelProperty("mess系统的token")
    private String messToken;

    @ApiModelProperty("真实名称")
    private String realName;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("部门信息")
    private String department;
}
