package com.steccn.portal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc 菜单权限DTO
 */
@Data
@ApiModel("菜单权限DTO-PermissionInfoDTO")
public class PermissionInfoDTO {
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("菜单编码")
    private String code;

    @ApiModelProperty("菜单名称")
    private String name;

    @ApiModelProperty("菜单图标")
    private String icon;

    @ApiModelProperty("菜单uri")
    private String uri;

    @ApiModelProperty("菜单host")
    private String host;

    @ApiModelProperty("应用名称")
    private String appName;
}
