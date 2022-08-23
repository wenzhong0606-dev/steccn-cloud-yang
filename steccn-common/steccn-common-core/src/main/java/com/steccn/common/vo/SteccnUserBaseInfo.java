package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc 凯盛节能的用户基础信息
 */
@Data
@ApiModel("凯盛节能的用户基础信息-SteccnUserBaseInfo")
public class SteccnUserBaseInfo {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户id")
    private String userName;

    @ApiModelProperty("角色集合")
    private List<String> permissionList;

}
