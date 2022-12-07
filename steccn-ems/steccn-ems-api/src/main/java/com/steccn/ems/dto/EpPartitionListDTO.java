package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EpPartitionListDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 能源分区树的主键id
     */
    private Long id;

    /**
     * 组织机构id
     */
    @NotNull(message = "组织机构id不能为空")
    private Integer officeId;

    /**
     * 组织机构名称
     */
    @NotBlank(message = "组织机构名称不能为空")
    private String officeName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 父id
     */
    private Integer parentId;

    /**
     * 
     */
    private Integer isDelete;
}
