package com.steccn.authserver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * oa跳过登录页token名
 *
 * @author AndrewLuo
 * @date 2022/08/29
 */
@Data
@ApiModel("oa单点登录跳转token")
public class OaSsoTokenResult {
    @ApiModelProperty("是否获取成功")
    private boolean status;
    @ApiModelProperty("token的具体值")
    private String token;
}
