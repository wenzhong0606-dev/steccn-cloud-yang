package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @desc 检验项停用启用
 */
@Data
@ApiModel("检验项停用启用dto")
public class QtItemStatusDTO implements Serializable {

    @ApiModelProperty("主键id")
    @NotNull(message = "主键不能为空")
    private Integer id;

    @ApiModelProperty("状态：0停用  1启用")
    @NotNull(message = "状态不能为空")
    @Max(value = 1, message = "状态只能是 0停用  1启用")
    @Min(value = 0, message = "状态只能是 0停用  1启用")
    private Integer status;
}
