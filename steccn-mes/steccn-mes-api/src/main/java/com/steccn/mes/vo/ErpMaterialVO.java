package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;

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

}
