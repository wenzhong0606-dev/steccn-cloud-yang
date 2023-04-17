package com.steccn.pms.vo;


import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiOperation("生产订单物料VO")
public class WorkOrderMaterialVO {


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
     * 批次号
     */
    private String batchNo;

    /**
     * 箱号
     */
    private String caseNo;
}
