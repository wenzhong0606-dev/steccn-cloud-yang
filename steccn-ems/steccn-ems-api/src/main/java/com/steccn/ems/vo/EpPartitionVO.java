package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class EpPartitionVO implements Serializable {
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

    /**
     * 是否关联仪表 0 否 1 是
     */
    private Integer isLinkTable;

    /**
     * 子级节点
     */
    private List<EpPartitionVO> children;
}
