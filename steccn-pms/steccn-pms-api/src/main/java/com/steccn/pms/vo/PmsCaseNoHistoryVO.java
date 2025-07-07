package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/04
 */
@Data
public class PmsCaseNoHistoryVO {
    /**
     * 箱号
     */
    private String caseNo;
    /**
     * 仓库
     */
    private Integer warehouseId;
    private String warehouseName;
    /**
     * 产线
     */
    private Integer productLineId;
    private String productLineName;
    /**
     * 关联单据
     */
    private String associatedNo;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 物料
     */
    private Long materialId;
    private String materialName;
    private String materialCode;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 单据类型
     */
    private Long typeId;
    private String typeName;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 货架id，货架名称
     */
    private String shelfId;
    private String shelfName;
}
