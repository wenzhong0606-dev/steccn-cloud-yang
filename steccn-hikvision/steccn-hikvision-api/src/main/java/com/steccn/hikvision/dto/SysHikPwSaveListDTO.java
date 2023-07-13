package com.steccn.hikvision.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 权限管理
 * @date 2023/7/4
 */
@Data
@ApiModel("海康威视权限保存dto")
public class SysHikPwSaveListDTO implements Serializable {

    @ApiModelProperty("0-表示是父节点 1-表示是叶子节点")
    @NotNull(message = "节点类型不能为空")
    private Integer type;

    @ApiModelProperty("视频监控code|虚拟节点code")
    @NotBlank(message = "视频监控不能为空")
    @Size(max = 128, message = "视频监控长度不能超过128个文字")
    private String indexCode;
    
}
