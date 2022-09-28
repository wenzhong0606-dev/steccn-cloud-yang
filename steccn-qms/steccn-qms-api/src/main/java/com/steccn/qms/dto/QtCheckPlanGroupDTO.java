package com.steccn.qms.dto;

import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 检验方案-检验分组DTO
 *
 * @author AndrewLuo
 * @date 2022/09/06
 */
@Data
public class QtCheckPlanGroupDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Integer id;

    @ApiModelProperty("分组名称")
    private String groupName;

    @ApiModelProperty("排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty("检验项DTO")
    private List<QtCheckPlanItemsDTO> items;
}
