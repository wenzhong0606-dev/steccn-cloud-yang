package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
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
    private Integer materialId;

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