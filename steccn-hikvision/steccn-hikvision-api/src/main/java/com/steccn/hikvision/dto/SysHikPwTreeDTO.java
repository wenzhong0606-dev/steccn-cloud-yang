package com.steccn.hikvision.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 海康威视
 * @date 2023/7/5
 */

@Data
@ApiModel("海康威视权限dto")
public class SysHikPwTreeDTO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("0-表示是父节点 1-表示是叶子节点")
    private Integer type;

    @ApiModelProperty("角色code")
    private String roleCode;

    @ApiModelProperty("视频监控code|节点code")
    private String indexCode;

    @ApiModelProperty("排序字段")
    private Integer sort;

    @ApiModelProperty("孩子节点")
    private List<SysHikPwTreeDTO> children;

    private static final long serialVersionUID = 1L;
}