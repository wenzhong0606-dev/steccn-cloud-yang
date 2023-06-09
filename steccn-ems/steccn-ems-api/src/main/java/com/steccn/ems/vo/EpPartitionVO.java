package com.steccn.ems.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
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
     * 父节点机构id
     */
    private Integer parentOfficeId;

    /**
     * 父节点机构名称
     */
    private String parentOfficeName;

    /**
     * 是否关联仪表 0 否 1 是
     */
    private Integer isLinkTable;

    @ApiModelProperty("子级节点id(根据该id 获取父级节点)")
    private Long childrenId;

    /**
     * 子级节点
     */
    private List<EpPartitionVO> children;

    /**
     * 表id
     */
    @JsonIgnore
    private Integer meterId;

    /**
     * 实体表类型
     */
    @JsonIgnore
    private Integer entityTable;

    @ApiModelProperty("远传仪表编码")
    private String distantMapCode;

    @ApiModelProperty("树的层级")
    private Integer level;

    /**
     * 0停用 1启用
     */
    @JsonIgnore
    private Integer meterStatus;
}
