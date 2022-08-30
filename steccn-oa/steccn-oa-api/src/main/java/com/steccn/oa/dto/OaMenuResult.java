package com.steccn.oa.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * oa系统菜单权限
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Data
@ApiModel("oa菜单返回值")
public class OaMenuResult extends Object{
    @ApiModelProperty("菜单id")
    private int id;
    @ApiModelProperty("菜单名")
    private String name;
    @ApiModelProperty("父级菜单id")
    private int pid;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("状态，成功为true，失败为false")
    private Boolean status;
    @ApiModelProperty("儿子节点")
    private List<OaMenuResult> child;
}
