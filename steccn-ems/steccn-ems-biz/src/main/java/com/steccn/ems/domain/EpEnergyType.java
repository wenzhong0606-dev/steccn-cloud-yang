package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 能源类型表
 * @TableName ep_energy_type
 */
@TableName(value ="ep_energy_type")
@Data
public class EpEnergyType implements Serializable {
    /**
     * 能源类型
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 能源类型名称
     */
    private String name;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 计量仪表名称
     */
    private String measureMeterName;

    /**
     * 关联图表
     */
    private List<Integer> linkTable;

    /**
     * 备注
     */
    private String note;

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