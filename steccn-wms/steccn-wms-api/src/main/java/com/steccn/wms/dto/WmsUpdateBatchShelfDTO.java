package com.steccn.wms.dto;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class WmsUpdateBatchShelfDTO {

    private Long id;

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
}
