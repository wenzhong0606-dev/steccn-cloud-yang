package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 类说明
 *
 */
@Data
@ApiModel("仓储仓库信息")
public class WmsStockVO implements Serializable {
    @ApiModelProperty("库存主键")
    private Long inventoryId;

    @ApiModelProperty("物料主键")
    private Long materialId;
    @ApiModelProperty("物料名称")
    private String materialName;
    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;
    @ApiModelProperty("总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("单位")
    private String unit;
    @ApiModelProperty("单位主键")
    private Long unitId;

    @ApiModelProperty("箱号")
    private String caseNo;
    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("仓库主键")
    private String warehouse;
    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架主键")
    private String shelf;
    @ApiModelProperty("货架名称")
    private String shelfName;
    private static final long serialVersionUID = 1L;
}
