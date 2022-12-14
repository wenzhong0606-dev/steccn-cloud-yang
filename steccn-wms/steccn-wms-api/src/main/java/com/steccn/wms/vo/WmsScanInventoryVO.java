package com.steccn.wms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓储库存
 *
 */
@Data
public class WmsScanInventoryVO implements Serializable {

    @ApiModelProperty("库存主键")
    private Long inventoryId;
    @ApiModelProperty("箱号")
    private String caseNo;
    @ApiModelProperty("批次号")
    private String batch;
    @ApiModelProperty("装箱时间")
    private LocalDateTime packTime;
    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("仓库")
    private Long warehouseId;
    @ApiModelProperty("仓库名称")
    private String warehouseName;
    @ApiModelProperty("货架id")
    private Long shelfId;
    @ApiModelProperty("货架名称")
    private String shelfName;
    @ApiModelProperty("物料id")
    private Long materialId;
    @ApiModelProperty("物料名称")
    private String materialName;
    @ApiModelProperty("出库状态")
    private Integer status;
    @ApiModelProperty("单位名称")
    private String unitName;

    private static final long serialVersionUID = 1L;
}