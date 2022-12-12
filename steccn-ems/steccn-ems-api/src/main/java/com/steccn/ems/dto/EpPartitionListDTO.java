package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EpPartitionListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 组织机构id
     */
    @NotNull(message = "组织机构id不能为空")
    private Integer id;

    /**
     * 组织机构名称
     */
    @NotBlank(message = "组织机构名称不能为空")
    private String officeName;

    /**
     * 排序字段
     */
    private Integer treeLevel;

    /**
     * 父级officeId
     */
    private Integer parentId;
}
