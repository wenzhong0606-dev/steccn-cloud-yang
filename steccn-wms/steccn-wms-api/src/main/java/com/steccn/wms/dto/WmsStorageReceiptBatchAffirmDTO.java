package com.steccn.wms.dto;


import lombok.Data;

import java.util.List;

@Data
public class WmsStorageReceiptBatchAffirmDTO {
    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 货架名程
     */
    private String shelfName;


    /**
     * 入库单id
     */
    private List<Long> idList;
}
