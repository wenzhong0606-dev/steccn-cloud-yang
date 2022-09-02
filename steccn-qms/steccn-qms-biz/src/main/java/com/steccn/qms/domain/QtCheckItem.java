package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * 检验项表
 *
 * @TableName qt_check_item
 */
@TableName(value = "qt_check_item")
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
     * 数据类型
     */
    private Integer dataType;

    /**
     * 计量单位
     */
    private Integer measureUnit;

    /**
     * 可能值
     */
    private Object possibleValue;

    /**
     * 默认值
     */
    private Object defaultValue;

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
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}