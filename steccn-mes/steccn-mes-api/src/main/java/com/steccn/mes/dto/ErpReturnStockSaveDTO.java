package com.steccn.mes.dto;

import lombok.Data;

/**
 * 销售退货单DTO
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class ErpReturnStockSaveDTO extends ErpCommonSaveDTO<ErpStockSaveDetailDTO>{
    /**
     * 客户编码
     */
    private String fcustno;
}
