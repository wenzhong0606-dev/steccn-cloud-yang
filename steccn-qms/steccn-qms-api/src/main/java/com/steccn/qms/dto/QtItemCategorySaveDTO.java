package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @desc 新增检验项|缺陷项类别
 */
@Data
@ApiModel("新增检验项|缺陷项类别")
public class QtItemCategorySaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空，编辑时存在")
    private Integer id;

    @ApiModelProperty("类别 0检验项 1缺陷项")
    @NotNull(message = "类型不能为空")
    private Integer type;

    @ApiModelProperty("名称")
    @NotBlank(message = "名称不能为空")
    @Length(max = 30, message = "名称长度不超过30个中文")
    private String name;
}
