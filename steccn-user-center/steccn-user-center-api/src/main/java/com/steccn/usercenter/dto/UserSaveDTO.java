package com.steccn.usercenter.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("保存用户信息")
public class UserSaveDTO {
    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户密码")
    private String password;

    @ApiModelProperty("移动电话")
    private String mobile;

    @ApiModelProperty("用户性别")
    private Byte gender;
}
