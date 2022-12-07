package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 能耗分区
 */
@Data
public class EpPartitionDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 能源类型id
     */
    @NotNull(message = "能源类型id不能为空")
    private Integer type;

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
     * 父节点id
     */
    private Long parentId;
}
