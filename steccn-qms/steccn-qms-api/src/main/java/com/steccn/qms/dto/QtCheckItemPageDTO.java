package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @desc 检验项分页请求
 */
@Data
@ApiModel("检验项列表请求dto")
public class QtCheckItemPageDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("检验项名称")
    private String name;

    @ApiModelProperty("类别id")
    @NotNull(message = "类别信息不能为空")
    private Integer categoryId;
}
