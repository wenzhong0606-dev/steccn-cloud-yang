package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @desc 新建检验项dto
 */
@Data
@ApiModel("新建检验项dto")
public class QtCheckItemSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Integer id;

    @ApiModelProperty("检验项名称")
    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "名称不能为空")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "名称长度不超过20个中文")
    private String name;

    @ApiModelProperty("数据类型")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "数据类型不能为空")
    private Long dataType;

    @ApiModelProperty("计量单位")
    private Long measureUnit;

    @ApiModelProperty("可能值")
    @Size(max = 100, message = "可能值选项个数最多100个")
    private List<String> possibleValue;

    @ApiModelProperty("默认值")
    private List<String> defaultValue;

    @ApiModelProperty("检验项类别id")
    @NotNull(groups = {InsertGroup.class}, message = "检验项类别不能为空")
    private Integer categoryId;

    @ApiModelProperty("操作说明")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 1000, message = "操作说明长度不超过1000个中文")
    private String opDesc;

    @ApiModelProperty("备注")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注长度不超过100个中文")
    private String note;

    @ApiModelProperty("检查方法")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "检查方法不超过50个中文")
    private String checkMethod;


}
