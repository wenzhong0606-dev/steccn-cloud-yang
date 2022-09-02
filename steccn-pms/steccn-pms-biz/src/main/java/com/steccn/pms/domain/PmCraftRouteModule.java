package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
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
     * 组件id
     */
    private Integer moduleId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 工序id
     */
    private Integer processId;

    /**
     * 是否固定用量（0：否   1：是）
     */
    private Integer ifFixedDosage;

    /**
     * 是否副产品（0：否   1：是）
     */
    private Integer ifSpinoff;

    /**
     * 是否联产品（0：否   1：是）
     */
    private Integer ifJoinProducts;

    /**
     * 是否反冲（0：否   1：是）
     */
    private Integer ifRecoil;

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