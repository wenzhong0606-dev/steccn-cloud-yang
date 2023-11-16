package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 客户条码vo
 * @author Mongknife
 * @date 2023/10/23
 */

@Data
@ApiModel("客户条码VO")
public class WmsCustomerBarcodeVO {

    //箱号
    private String caseNo;

    //生产批次号
    private String batchNo;

    //生产订单号
    private String produceOrderCode;

    //物料名称
    private String  materialName;

    //物料编码
    private String materialCode;

    //物料id
    private Long materialId;




}
