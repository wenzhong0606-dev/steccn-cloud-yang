package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 生产工艺路线

 * @TableName pm_craft_route
 */
@TableName(value ="pm_craft_route")
@Data
public class PmCraftRoute implements Serializable {

    /**
     * 表ID
     */
    @TableId
    private Long id;

    /**
     * 工艺编码
     */
    private String code;

    /**
     * 物料ID
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;
    /**
     * 工艺路线状态：1有效0无效
     */
    private Integer status;

    /**
     * 版本
     */
    private String version;

    /**
     * 是否当前版本：1是0否
     */
    private Integer isCurrentVersion;

    /**
     * 有效开始时间
     */
    private LocalDateTime validBeginTime;

    /**
     * 有效结止时间
     */
    private LocalDateTime validEndTime;

    /**
     * 备注
     */
    private String note;

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
     * 工艺路线图
     */
    private String roadmap;


}