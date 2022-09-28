package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工艺路线-组件
 * @TableName pm_craft_route_module
 */
@TableName(value ="pm_craft_route_module")
@Data
public class PmCraftRouteModule implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 物料组件id
     */
    private Long materialId;


    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 工序名称
     */
    private String processName;
    /**
     * 工艺路线id
     */
    private Long craftRouteId;

    /**
     * 是否固定用量（0：否   1：是）
     */
    private Integer isFixedQuantity;

    /**
     * 是否副产品（0：否   1：是）
     */
    private Integer isCopruduct;

    /**
     * 是否联产品（0：否   1：是）
     */
    private Integer isJointProduct;

    /**
     * 是否反冲（0：否   1：是）
     */
    private Integer isBackCalculation;

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
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;


    /**
     * 工序报废百分比
     */
    private Float processScrapingPercent;

    /**
     * 组件报废百分比
     */
    private Float componentScrapingPercent;

}