package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * erp物料VO
 *
 * @author AndrewLuo
 * @date 2023/08/09
 */
@Data
public class ErpMaterialVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 物料id
     */
    private Integer fmaterialid;
    /**
     * 物料编码
     */
    private String fnumber;
    /**
     * 物料名称
     */
    private String fname;
    /**
     * 规格型号
     */
    private String fspecification;
    /**
     * 物料分组id
     */
    private String fmaterialgroupid;
    /**
     * 物料分组编码
     */
    private String fmaterialgroupno;
    /**
     * 是否启用批号
     */
    private String fislot;
    /**
     * 基本单位编码
     */
    private String fbaseunitno;
    /**
     * 基本单位名称
     */
    private String fbaseunitname;
    /**
     * 长
     */
    private BigDecimal flength;
    /**
     * 宽
     */
    private BigDecimal fwidth;
    /**
     * 厚
     */
    private BigDecimal fland;
    /**
     * 尺寸单位编码
     */
    private String fvolumeno;
    /**
     * 尺寸单位明细
     */
    private String fvolumename;
    /**
     * 尺寸单位换算率
     */
    private BigDecimal fvolumerate;

}
