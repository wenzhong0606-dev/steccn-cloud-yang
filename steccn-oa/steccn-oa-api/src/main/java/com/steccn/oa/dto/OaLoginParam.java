package com.steccn.oa.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * oa请求账号密码
 *
 * @author AndrewLuo
 * @date 2022/08/26
 */
@Data
@ApiModel("Oa登录参数")
public class OaLoginParam {
    @ApiModelProperty("用户名")
    private String loginid;
    @ApiModelProperty("密码")
    private String userpassword;
}
