package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("历史库存vo")
public class WmsHistoricalInventoryVO {

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private  String warehouseName;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("期初库存")
    private BigDecimal openingInventory;

    @ApiModelProperty("期末库存")
    private BigDecimal endingInventory;

    @ApiModelProperty("入库单数")
    private Integer warehousingEntry;

    @ApiModelProperty("入库总数")
    private BigDecimal entryTotalQuantity;

    @ApiModelProperty("出库单数")
    private Integer warehousingOutbound;

    @ApiModelProperty("出库总数")
    private BigDecimal warehousingOutboundTotalQuantity;

    @ApiModelProperty("调入单据数量")
    private Integer transferDocument;

    @ApiModelProperty("调入数量")
    private BigDecimal transferQuantity;

    @ApiModelProperty("调出单据数量")
    private Integer transferLeaveDocument;

    @ApiModelProperty("调出数量")
    private BigDecimal transferLeaveQuantity;

    private String unit;


}
