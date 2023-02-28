package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 用户和移动端client绑定
 */
@Data
@ApiModel("用户和移动端client绑定")
public class SysUserClientRefDTO implements Serializable {

    @ApiModelProperty("用户id")
    @NotNull(message = "用户主键不能为空")
    private Long userId;

    @ApiModelProperty("用户名称")
    @NotNull(message = "用户名称不能为空")
    private String username;

    @ApiModelProperty("用户常用的客户端code")
    @NotNull(message = "用户常用的客户端code不能为空")
    private String clientNo;

}
