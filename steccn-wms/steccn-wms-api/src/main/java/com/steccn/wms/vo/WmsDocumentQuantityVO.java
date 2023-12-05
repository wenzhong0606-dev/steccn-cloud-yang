package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("单据数量")
public class WmsDocumentQuantityVO implements Serializable {


    /**
     * 入库单数
     */
    private Integer inboundSingular;

    /**
     * 出库单数
     */
    private Integer outboundSingular;

    /**
     * 调拨单数
     */
    private Integer dialingSingular;
}
