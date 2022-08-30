package com.steccn.authserver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Oa跳过登录页
 *
 * @author AndrewLuo
 * @date 2022/08/29
 */
@Data
@ApiModel("Oa跳转登录页传入参数")
public class OaSsoTokenParam {
    @ApiModelProperty("用户名")
    private String loginid;
    @ApiModelProperty("appid默认ssso")
    private String appid;
}
