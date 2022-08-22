package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel("用户基本信息-UserBaseInfoVO")
@Data
public class UserBaseInfoVO {

    @ApiModelProperty("用户id")
    private String userName;

    @ApiModelProperty("角色集合")
    private List<String> permissionList;

    @ApiModelProperty("token")
    private String accessToken;

    @ApiModelProperty("过期时间")
    private LocalDateTime expiresAt;

    @ApiModelProperty("刷新token")
    private String refreshToken;
}
