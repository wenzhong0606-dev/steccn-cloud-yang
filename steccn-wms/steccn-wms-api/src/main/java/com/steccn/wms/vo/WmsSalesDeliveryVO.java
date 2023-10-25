package com.steccn.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售系统送货单VO
 *
 * @author AndrewLuo
 * @date 2023/09/14
 */
@Data
public class WmsSalesDeliveryVO implements Serializable {
    /**
     * 送货单编码
     */
    private String deliveryOrderCode;
    /**
     * 物料id
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
     * 规格型号
     */
    private String specifications;
    /**
     * 箱号
     */
    private String caseNo;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 单位id
     */
    private Long unitId;
    /**
     * 单位名称
     */
    private String unitName;
    /**
     * 关联单据
     */
    private String associatedNo;
    /**
     * 供应商编码
     */
    private String esSupplierCode;
    /**
     * 外部系统来源订单头编号
     */
    private String esFromPoNum;
    /**
     * 外部系统来源订单行编号
     */
    private String esFromPoLineNum;
}
