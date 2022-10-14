package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 检验项表
 *
 * @TableName qt_check_item
 */
@TableName(value = "qt_check_item", autoResultMap = true)
@Data
public class QtCheckItem implements Serializable {
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
     * 检验项名称
     */
    private String name;


    /**
     * 检验项名称全拼
     */
    private String nameEn;

    /**
     * 数据类型
     */
    private Long dataType;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 可能值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> possibleValue;

    /**
     * 默认值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> defaultValue;

    /**
     * 检验项类别id
     */
    private Integer categoryId;

    /**
     * 状态：0停用  1启用
     */
    private Integer status;

    /**
     * 操作说明
     */
    private String opDesc;

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

    /**
     * 检查方法
     */
    private String checkMethod;
}