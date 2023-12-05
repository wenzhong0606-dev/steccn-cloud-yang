package com.steccn.wms.vo;


import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiOperation("仓储驾驶仓单据明细总数")
public class WmsReportTotalQuantityVO {

    /**
     * 入库明细总数
     */
    private BigDecimal warehouseTotalQuantity;

    /**
     * 出库明细总数
     */
    private BigDecimal outboundTotalQuantity;

    /**
     * 调拨单明细总数
     */
    private BigDecimal transferTotalQuantity;

}
