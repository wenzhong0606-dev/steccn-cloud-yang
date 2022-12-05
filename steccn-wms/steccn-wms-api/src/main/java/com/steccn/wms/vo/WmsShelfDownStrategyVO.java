package com.steccn.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 下架策略
 * @TableName wms_shelf_down_strategy
 */
@Data
public class WmsShelfDownStrategyVO implements Serializable {

    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 下架类型
     */
    private Long soldOutType;

    private String soldOutTypeName;//下架类型名称

    /**
     * 货架排序
     */
    private Long shelfOrder;

    private String shelfOrderName;//货架排序名称

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

    private static final long serialVersionUID = 1L;

    private String note;//备注
}