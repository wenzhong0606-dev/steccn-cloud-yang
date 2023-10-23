package com.steccn.pms.dto;


import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.util.List;

@Data
public class PanoramicTracingDTO  extends BasePageDTO {



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
     * 客户条码
     */
    private String customerBarcode;

    private List<String> caseNos;


}
