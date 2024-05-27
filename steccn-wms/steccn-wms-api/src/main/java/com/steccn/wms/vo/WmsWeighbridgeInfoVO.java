package com.steccn.wms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地磅VO
 *
 * @author AndrewLuo
 * @date 2024/05/16
 */
@Data
public class WmsWeighbridgeInfoVO implements Serializable {
    private Long id;
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
     * 流程单号
     */
    private String flowCode;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * 联系方式
     */
    private String driverPhone;

    /**
     * 供应商id
     */
    private String companyId;

    /**
     * 供应商名称
     */
    private String companyName;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 皮重
     */
    private BigDecimal tareWeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 是否生成入库单 0否 1是
     */
    private Integer isGenerateList;

    /**
     * 入库单号
     */
    private String receiptCode;
}
