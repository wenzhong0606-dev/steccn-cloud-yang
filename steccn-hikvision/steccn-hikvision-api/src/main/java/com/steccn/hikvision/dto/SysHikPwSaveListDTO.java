package com.steccn.hikvision.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 权限管理
 * @date 2023/7/4
 */
@Data
@ApiModel("海康威视权限保存dto")
public class SysHikPwSaveListDTO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("0-表示是父节点 1-表示是叶子节点")
    private Integer type;

    @ApiModelProperty("角色code")
    private String roleCode;

    @ApiModelProperty("视频监控code|节点code")
    private Long indexCode;

    @ApiModelProperty("排序字段")
    private Integer sort;
}
