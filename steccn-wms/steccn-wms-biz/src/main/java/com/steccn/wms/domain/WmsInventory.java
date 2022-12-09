package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 仓储库存
 * @TableName wms_inventory
 */
@TableName(value ="wms_inventory")
@Data
public class WmsInventory implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 货架id
     */
    private Long shelfId;

    /**
     * 总数
     */
    private BigDecimal totalQuantity;

    /**
     * 可用数量
     */
    private BigDecimal availableQuantity;

    /**
     * 在途库存
     */
    private BigDecimal stockQuantity;

    /**
     * 冻结库存
     */
    private BigDecimal freezeQuantity;

    /**
     * 锁定库存
     */
    private BigDecimal lockQuantity;

    /**
     * 质检库存
     */
    private BigDecimal detectionQuantity;

    /**
     * 单位id
     */
    private Long unit;

    /**
     * 箱号
     */
    private String caseno;

    /**
     * 批次
     */
    private String batch;

    /**
     * 打包时间
     */
    private LocalDateTime packTime;

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
    private Integer delFlag;

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}