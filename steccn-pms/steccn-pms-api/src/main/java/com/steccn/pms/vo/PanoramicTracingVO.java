package com.steccn.pms.vo;


import lombok.Data;

@Data
public class PanoramicTracingVO {


    /**
     * id
     */
    private Long id;

    /**
     * 生产订单号
     */
    private String code;


    /**
     * 生产批次号
     */
    private String batchNo;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;
}
