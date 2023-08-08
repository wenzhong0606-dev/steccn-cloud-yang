package com.steccn.mes.dto;


import lombok.Data;

/**
 * 采购入库单
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class ErpStockSaveDTO extends ErpCommonSaveDTO<ErpStockSaveDetailDTO>{
    /**
     * 供应商编码
     */
    private String fsuppno;
}
