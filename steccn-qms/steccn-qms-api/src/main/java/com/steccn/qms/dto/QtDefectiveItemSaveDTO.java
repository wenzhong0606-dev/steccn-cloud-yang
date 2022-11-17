package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @desc 缺陷项保存dto
 */
@Data
@ApiModel("缺陷项保存dto")
public class QtDefectiveItemSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Integer id;

    @ApiModelProperty("缺陷项名称")
    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "名称不能为空")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "名称长度不超过20个中文")
    private String name;

    @ApiModelProperty("缺陷项备注")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "缺陷项备注长度不超过100个中文")
    private String note;

    @ApiModelProperty("缺陷项类别id")
    @NotNull(groups = InsertGroup.class, message = "缺陷项类别id不能为空")
    private Integer categoryId;

    @ApiModelProperty("缺陷项类别")
    @NotNull(groups = InsertGroup.class, message = "缺陷项类别不能为空")
    private String defectCategory;
}
