package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 收发货信息
 * @TableName pms_logistics_info
 */
@TableName(value ="pms_logistics_info")
@Data
public class PmsLogisticsInfo implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 物流单id
     */
    private Long logisticsId;

    /**
     * 收发货类型（0：收 1：发）
     */
    private Integer type;

    /**
     * 机构id
     */
    private Integer officeId;

    /**
     * 机构名称
     */
    private String officeName;

    /**
     * 地址
     */
    private String site;

    /**
     * 联系人
     */
    private String linkman;

    /**
     * 联系方式
     */
    private String contactWay;

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