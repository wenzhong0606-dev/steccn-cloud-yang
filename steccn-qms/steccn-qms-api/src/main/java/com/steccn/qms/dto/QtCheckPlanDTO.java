package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 检验方案dto
 *
 * @author AndrewLuo
 * @date 2022/09/06
 */
@Data
@ApiModel("检验方案dto")
public class QtCheckPlanDTO {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Integer id;

    @ApiModelProperty("检验方案名称")
    private String name;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("检验方式")
    private Integer checkMethod;

    @ApiModelProperty("检验分组")
    private List<QtCheckPlanGroupDTO> checkGroup;

    @ApiModelProperty("检验项DTO")
    private List<QtCheckPlanItemsDTO> items;
}
