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
     * 能耗分区名称
     */
    private String partitionName;

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
     * 父级节点名称
     */
    private String parentName;

    /**
     * 子级节点
     */
    private List<EpPartitionVO> children;
}
