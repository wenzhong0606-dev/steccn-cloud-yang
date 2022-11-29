package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 能耗分区
 * @TableName ep_partition
 */
@TableName(value ="ep_partition")
@Data
public class EpPartition implements Serializable {
    /**
     * 主键
     */
    @TableId
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
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}