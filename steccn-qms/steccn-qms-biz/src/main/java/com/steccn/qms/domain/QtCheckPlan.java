package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * 检验方案表
 *
 * @TableName qt_check_plan
 */
@TableName(value = "qt_check_plan")
@Data
public class QtCheckPlan implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 编码
     */
    private String code;

    /**
     * 检验方案名称
     */
    private String name;

    /**
     * 状态：0停用 1启用
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
     * 版本号-每次调整都需要+1
     */
    private Long version;

    /**
     * 快照数据存放原始主键id
     */
    private Integer originalId;

    /**
     * 检验方式
     * 检验方式：0计量检验  1计数检验
     */
    private Integer checkMethod;

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