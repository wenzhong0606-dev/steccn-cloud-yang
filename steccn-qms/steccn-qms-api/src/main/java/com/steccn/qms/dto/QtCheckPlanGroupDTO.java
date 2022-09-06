package com.steccn.qms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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

    @ApiModelProperty("分组名称")
    private String groupName;

    @ApiModelProperty("检验项DTO")
    private List<QtCheckPlanItemsDTO> items;
}
