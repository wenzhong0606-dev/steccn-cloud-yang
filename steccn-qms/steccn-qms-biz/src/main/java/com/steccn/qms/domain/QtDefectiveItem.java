package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 缺陷项表
 * @TableName qt_defective_item
 */
@TableName(value ="qt_defective_item")
@Data
public class QtDefectiveItem implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 编码
     */
    private String code;

    /**
     * 缺陷项名称
     */
    private String name;

    /**
     * 缺陷项名称
     */
    private String nameEn;

    /**
     * 缺陷项类别id
     */
    private Integer categoryId;

    /**
     * 缺陷项类别编码
     */
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "缺陷项类别编码不能超过20个字符")
    private String defectCode;

    /**
     * 状态：0停用  1启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String note;

    /**
     * 是否可见：0否 1是 0表示是快照
     */
    private Integer showFlag;

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