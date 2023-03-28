package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生产领料单
 *
 * @author yequan
 * @TableName pms_material_requisition
 */
@TableName(value = "pms_material_requisition")
@Data
public class PmsMaterialRequisition implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型id
     */
    private Long typeId;

    /**
     * 状态
     */
    private Long status;

    /**
     * 部门id
     */
    private Integer departmentId;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 关联单据
     */
    private String associatedNo;

    /**
     * 备注
     */
    private String note;

    /**
     * 确认人
     */
    private String affirmName;

    /**
     * 确认人id
     */
    private Integer affirmId;

    /**
     * 确认时间
     */
    private LocalDateTime affirmTime;

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

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}