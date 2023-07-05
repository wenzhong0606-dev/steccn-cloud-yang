package com.steccn.hikvision.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 海康威视数据权限保存
 * @date 2023/7/5
 */
@Data
@ApiModel("海康威视权限保存dto")
public class SysHikPwSaveDTO implements Serializable {

    @ApiModelProperty("角色")
    @NotBlank(message = "角色不能为空")
    @NotNull(message = "角色不能为空")
    @Size(max = 64, message = "角色长度不能超过64个文字")
    private String roleCode;

    @ApiModelProperty("权限集合")
    @Valid
    private List<SysHikPwSaveListDTO> pwList;

}
