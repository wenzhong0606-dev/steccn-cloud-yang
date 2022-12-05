package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EpPartitionDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 能源类型id
     */
    private Integer type;

    /**
     * 组织机构id
     */
    private Integer officeId;

    /**
     * 组织机构名称
     */
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
