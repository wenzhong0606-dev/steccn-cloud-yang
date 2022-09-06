package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    private Integer id;

    @ApiModelProperty("检验方案名称")
    private String name;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("检验分组")
    private List<QtCheckPlanGroupDTO> checkGroup;
}
