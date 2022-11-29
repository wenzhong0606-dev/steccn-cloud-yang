package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 盘点单
 * @TableName pms_check_order
 */
@TableName(value ="pms_check_order")
@Data
public class PmsCheckOrder implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private Long type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 盘点岗位
     */
    private Integer post;

    /**
     * 盘点开始时间
     */
    private LocalDateTime checkTime;

    /**
     * 盘点范围（0：仓库 1：货架 2：物料分类 3：物料）
     */
    private Integer checkScope;

    /**
     * 盘点区域
     */
    private Object checkArea;

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